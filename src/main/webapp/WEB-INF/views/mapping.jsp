<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<html>
<head><title>Mapping trouve</title></head>
<body>
<table border="1">
<tr><th>Controller</th><th>URL</th><th>HTTP</th><th>Methode</th></tr>
<tr>
    <td><%= request.getAttribute("controllerClass") %></td>
    <td><%= request.getAttribute("url") %></td>
    <td><%= request.getAttribute("httpMethod") %></td>
    <td><%= request.getAttribute("methodName") %>()</td>
</tr>
</table>
<hr/>
<h3>Resultat :</h3>
<p><%= request.getAttribute("result") %></p>
</body>
</html>
