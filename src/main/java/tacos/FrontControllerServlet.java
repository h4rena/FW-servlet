package main.java.tacos;

import java.io.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

import main.java.model.ModelAndView;
import main.java.model.UrlMethod;
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
            res.setContentType("text/html;charset=UTF-8");
            PrintWriter out = res.getWriter();

            out.println("<table border='1'>");
            out.println("<tr><th>Controller</th><th>URL</th><th>UrlMethod</th><th>Methode</th></tr>");
            out.println("<tr>");
            out.println("<td>" + method.getDeclaringClass().getName() + "</td>");
            out.println("<td>" + path + "</td>");
            out.println("<td>" + httpMethod + "</td>");
            out.println("<td>" + method.getName() + "()</td>");
            out.println("</tr></table>");

            try {
                Object controller = controllerInstances.get(method.getDeclaringClass().getName());
                Object result = method.invoke(controller);

                if (result instanceof ModelAndView) {
                    ModelAndView mv = (ModelAndView) result;
                    String view = mv.getView();
                    for (Map.Entry<String, Object> e : mv.getData().entrySet())
                        req.setAttribute(e.getKey(), e.getValue());
                    req.getRequestDispatcher("/WEB-INF/views/" + view + ".jsp").forward(req, res);
                    return;
                }

                out.println("method invoque : " + "<p>" + result + "</p>");

            } catch (Exception e) {
                throw new ServletException("Erreur invocation de " + method.getName(), e);
            }
            
        } else {
            res.setContentType("text/html;charset=UTF-8");
            PrintWriter out = res.getWriter();
            out.println("<p style='color:red'>Aucune methode pour " + httpMethod + " " + path + "</p>");
            out.println("<hr/><h3>Tous les URL mappings disponibles :</h3>");
            out.println("<table border='1'>");
            out.println("<tr><th>Controller</th><th>URL</th><th>UrlMethod</th><th>Methode</th></tr>");
            if (urlMappings != null) {
                for (Map.Entry<UrlMethod, Method> entry : urlMappings.entrySet()) {
                    Method m = entry.getValue();
                    UrlMethod key = entry.getKey();
                    out.println("<tr>");
                    out.println("<td>" + m.getDeclaringClass().getName() + "</td>");
                    out.println("<td>" + key.getUrl() + "</td>");
                    out.println("<td>" + key.getMethod() + "</td>");
                    out.println("<td>" + m.getName() + "()</td>");
                    out.println("</tr>");
                }
            }
            out.println("</table>");
        }
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException,IOException {
        processRequest(req, res);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException,IOException {
        processRequest(req, res);
    }

}
