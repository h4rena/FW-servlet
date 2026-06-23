package main.java.tacos;

import java.io.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

import main.java.utils.scanController;

public class FrontControllerServlet extends HttpServlet {

    private Map<String, Method> urlMappings;
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
                urlMappings.putAll(scanController.getUrlMappings(clazz));
            }
        } catch (Exception e) {
            throw new ServletException("Erreur initialisation registre URL", e);
        }
    }

    protected Method getMethodForUrl(String path) {
        return urlMappings != null ? urlMappings.get(path) : null;
    }

    protected Map<String, Method> getAllMappings() {
        return urlMappings;
    }

    protected Object getControllerInstance(String path) {
        Method m = getMethodForUrl(path);
        if (m == null || controllerInstances == null) return null;
        return controllerInstances.get(m.getDeclaringClass().getName());
    }

    public void processRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String path = req.getPathInfo();
        res.setContentType("text/html;charset=UTF-8");

        PrintWriter out = res.getWriter();

        Method method = getMethodForUrl(path);

        if (method != null) {
            out.println("<table border='1'>");
            out.println("<tr><th>Controller</th><th>URL</th><th>Methode</th></tr>");
            out.println("<tr>");
            out.println("<td>" + method.getDeclaringClass().getName() + "</td>");
            out.println("<td>" + path + "</td>");
            out.println("<td>" + method.getName() + "()</td>");
            out.println("</tr>");
            out.println("</table>");
        } else {
            out.println("<p style='color:red'>Aucune methode pour l'URL : " + path + "</p>");
            out.println("<hr/><h3>Tous les URL mappings disponibles :</h3>");
            out.println("<table border='1'>");
            out.println("<tr><th>Controller</th><th>URL</th><th>Methode</th></tr>");
            if (urlMappings != null) {
                for (Map.Entry<String, Method> entry : urlMappings.entrySet()) {
                    Method m = entry.getValue();
                    out.println("<tr>");
                    out.println("<td>" + m.getDeclaringClass().getName() + "</td>");
                    out.println("<td>" + entry.getKey() + "</td>");
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
