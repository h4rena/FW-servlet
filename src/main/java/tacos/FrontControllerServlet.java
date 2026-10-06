package main.java.tacos;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import main.java.annotation.WebApi;
import main.java.model.ModelAndView;
import main.java.model.UrlMethod;
import main.java.utils.scanController;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FrontControllerServlet extends HttpServlet {

    private Map<UrlMethod, Method> urlMappings;
    private Map<String, Object> controllerInstances;
    private static final ObjectMapper MAPPER = new ObjectMapper();

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
            try {
                Object controller = controllerInstances.get(method.getDeclaringClass().getName());
                Object[] args = bind(req, method);
                Object result = method.invoke(controller, args);

                WebApi api = method.getAnnotation(WebApi.class);   // <- la détection
                if (api != null) {
                    res.setContentType("application/json;charset=UTF-8");
                    PrintWriter out = res.getWriter();
                    out.write(api.toJson() ? MAPPER.writeValueAsString(result) : String.valueOf(result));
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
            req.getRequestDispatcher("/WEB-INF/views/error_mapping.jsp").forward(req, res);
        }
    }

    protected Object[] bind(HttpServletRequest req, Method method) {
        Parameter[] params = method.getParameters();
        Object[] args = new Object[params.length];

        for (int i = 0; i < params.length; i++) {
            String nom = nomParametre(params[i], method);
            String valeur = req.getParameter(nom);
            Class<?> type = params[i].getType();

            if (type == String.class) {
                args[i] = valeur;
            } else if (type == int.class) {
                if (valeur == null || valeur.isBlank()) {
                    args[i] = Integer.valueOf(0);
                } else {
                    try {
                        args[i] = Integer.valueOf(valeur);
                    } catch (NumberFormatException nfe) {
                        throw new RuntimeException(
                            "Parametre '" + nom + "' de la methode " + method.getName()
                            + " : '" + valeur + "' n'est pas un entier");
                    }
                }
            } else if (type == boolean.class) {
                args[i] = "true".equalsIgnoreCase(valeur)
                        || "on".equalsIgnoreCase(valeur)
                        || "1".equals(valeur);
            } else {
                throw new RuntimeException(
                    "Type de parametre non supporté par bind : " + type.getName()
                    + " (methode " + method.getName() + ", parametre " + i + ")");
            }
        }

        return args;
    }

    private String nomParametre(Parameter p, Method method) {
        if (!p.isNamePresent())
            throw new RuntimeException(
                "Nom du parametre indisponible pour " + method.getName()
                + " : recompiler avec -parameters");
        return p.getName();
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException,IOException {
        processRequest(req, res);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException,IOException {
        processRequest(req, res);
    }

}
