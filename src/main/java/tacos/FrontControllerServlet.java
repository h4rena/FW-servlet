package main.java.tacos;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import main.java.annotation.RequireAuth;
import main.java.annotation.WebApi;
import main.java.model.ModelAndView;
import main.java.model.Session;
import main.java.model.UrlMethod;
import main.java.utils.Binder;
import main.java.utils.JsonSerializer;
import main.java.utils.scanController;

public class FrontControllerServlet extends HttpServlet {

    private Map<UrlMethod, Method> urlMappings;
    private Map<String, Object> controllerInstances;

    public void init() throws ServletException {
        String pkg = getServletContext().getInitParameter("controllers-package");
        if (pkg == null) pkg = "main.java.controllers";
        try {
            List<String> controllers = new scanController().scanControllers(pkg);
            urlMappings = new HashMap<>();
            controllerInstances = new HashMap<>();

            for (String name : controllers) {
                Class<?> clazz = Class.forName(name);
                Object instance = clazz.getDeclaredConstructor().newInstance();
                //session injection
                injecterSession(instance);
                controllerInstances.put(name, instance);

                Map<UrlMethod, Method> classMappings = scanController.getUrlMappings(clazz);
                for (Map.Entry<UrlMethod, Method> entry : classMappings.entrySet()) {
                    if (urlMappings.containsKey(entry.getKey())) {
                        Method existing = urlMappings.get(entry.getKey());
                        throw new RuntimeException(
                            "UrlMapping dupliqué : " + entry.getKey() +
                            " (déjà déclaré dans " + existing.getDeclaringClass().getName() +
                            "." + existing.getName() +
                            ") en conflit avec " + entry.getValue().getDeclaringClass().getName() +
                            "." + entry.getValue().getName()
                        );
                    }
                    urlMappings.put(entry.getKey(), entry.getValue());
                }
            }

            getServletContext().setAttribute("urlMappings", urlMappings);
        } catch (RuntimeException e) {
            getServletContext().setAttribute("initError", e.getMessage());
        } catch (Exception e) {
            throw new ServletException("Erreur initialisation registre URL", e);
        }
    }

    protected Method getMethodForUrl(String path, String httpMethod) {
        if (urlMappings == null) return null;
        return urlMappings.get(new UrlMethod(path, httpMethod));
    }

    protected Map<UrlMethod, Method> getAllMappings() {
        return urlMappings;
    }

    protected Object getControllerInstance(String path, String httpMethod) {
        Method m = getMethodForUrl(path, httpMethod);
        if (m == null || controllerInstances == null) return null;
        return controllerInstances.get(m.getDeclaringClass().getName());
    }

    //session injection
    private void injecterSession(Object controller) {
        Session session = (Session) getServletContext().getAttribute("session");
        if (session == null) return;

        for (Field f : controller.getClass().getDeclaredFields()) {
            if (f.getType() == Session.class) {
                try {
                    f.setAccessible(true);
                    f.set(controller, session);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(
                        "Injection de la session impossible dans "
                        + controller.getClass().getName(), e);
                }
            }
        }
    }

    private void gererNonAuthentifie(HttpServletRequest req, HttpServletResponse res,
                                    Method method) throws ServletException, IOException {
        if (method.isAnnotationPresent(WebApi.class)) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"erreur\":\"Non authentifie\"}");
        } else {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            req.setAttribute("url", req.getPathInfo());
            req.getRequestDispatcher("/WEB-INF/views/erreur_401.jsp").forward(req, res);
        }
    }

    private void gererAccesRefuse(HttpServletRequest req, HttpServletResponse res,
                                 Method method, String roleRequis) throws ServletException, IOException {
        res.setStatus(HttpServletResponse.SC_FORBIDDEN);
        if (method.isAnnotationPresent(WebApi.class)) {
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"erreur\":\"Acces refuse\"}");
        } else {
            Session session = (Session) getServletContext().getAttribute("session");
            Object roleActuel = (session != null) ? session.get("role") : null;
            req.setAttribute("url", req.getPathInfo());
            req.setAttribute("roleRequis", roleRequis);
            req.setAttribute("roleActuel", roleActuel != null ? roleActuel.toString() : "aucun");
            req.getRequestDispatcher("/WEB-INF/views/erreur_403.jsp").forward(req, res);
        }
    }

    public void processRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String error = (String) getServletContext().getAttribute("initError");
        if (error != null) {
            req.setAttribute("error", error);
            req.getRequestDispatcher("/error.jsp").forward(req, res);
            return;
        }

        String path = req.getPathInfo();
        String httpMethod = req.getMethod();

        Method method = getMethodForUrl(path, httpMethod);

        if (method != null) {
            RequireAuth auth = method.getAnnotation(RequireAuth.class);
            if (auth != null) {
                res.setHeader("Cache-Control", "no-store");
                Session session = (Session) getServletContext().getAttribute("session");
                if (session == null || !session.estConnecte()) {
                    gererNonAuthentifie(req, res, method);
                    return;
                }
                if (!auth.role().isEmpty() && !session.aRole(auth.role())) {
                    gererAccesRefuse(req, res, method, auth.role());
                    return;
                }
            }

            try {
                Object controller = controllerInstances.get(method.getDeclaringClass().getName());
                Object[] args = Binder.bind(req, method);
                Object result = method.invoke(controller, args);

                WebApi api = method.getAnnotation(WebApi.class);   // <- la détection
                if (api != null) {
                    res.setContentType("application/json;charset=UTF-8");
                    PrintWriter out = res.getWriter();
                    out.write(api.toJson() ? JsonSerializer.serialize(result) : String.valueOf(result));
                    return;                                        // <- pas de RequestDispatcher
                }

                if (result instanceof ModelAndView) {
                    ModelAndView mv = (ModelAndView) result;
                    for (Map.Entry<String, Object> e : mv.getData().entrySet())
                        req.setAttribute(e.getKey(), e.getValue());
                    req.getRequestDispatcher("/WEB-INF/views/" + mv.getView() + ".jsp").forward(req, res);
                    return;
                }

                req.setAttribute("controllerClass", method.getDeclaringClass().getName());
                req.setAttribute("url", path);
                req.setAttribute("httpMethod", httpMethod);
                req.setAttribute("methodName", method.getName());
                req.setAttribute("result", result);
                req.getRequestDispatcher("/WEB-INF/views/mapping.jsp").forward(req, res);

            } catch (Exception e) {
                throw new ServletException("Erreur invocation de " + method.getName(), e);
            }

        } else {
            req.setAttribute("requestedUrl", path);
            req.setAttribute("requestedMethod", httpMethod);
            req.setAttribute("allMappings", urlMappings);

            String[] allowed = allowedMethods(path);
            if (allowed.length > 0) {
                res.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                res.setHeader("Allow", String.join(", ", allowed));
            } else {
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
            req.getRequestDispatcher("/WEB-INF/views/error_mapping.jsp").forward(req, res);
        }
    }

    private String[] allowedMethods(String path) {
        if (urlMappings == null) return new String[0];
        List<String> methods = new java.util.ArrayList<>();
        for (UrlMethod key : urlMappings.keySet()) {
            if (key.getUrl().equals(path)) {
                methods.add(key.getMethod());
            }
        }
        return methods.toArray(new String[0]);
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException,IOException {
        processRequest(req, res);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException,IOException {
        processRequest(req, res);
    }

}