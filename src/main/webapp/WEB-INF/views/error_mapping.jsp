<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
    import="java.util.Map, java.lang.reflect.Method, main.java.model.UrlMethod" %>
<html>
<head><title>Mapping introuvable</title></head>
<body>
<p style="color:red">Aucune methode pour <%= request.getAttribute("requestedMethod") %> <%= request.getAttribute("requestedUrl") %></p>
<hr/>
<h3>Tous les URL mappings disponibles :</h3>
<table border="1">
<tr><th>Controller</th><th>URL</th><th>HTTP</th><th>Methode</th></tr>
<%
    Map<UrlMethod, Method> allMappings = (Map<UrlMethod, Method>) request.getAttribute("allMappings");
    if (allMappings != null) {
        for (Map.Entry<UrlMethod, Method> entry : allMappings.entrySet()) {
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
%>
</table>
</body>
</html>
