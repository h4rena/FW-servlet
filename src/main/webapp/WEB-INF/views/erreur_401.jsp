<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<html>
<head><title>Acces refuse - non connecte</title></head>
<body>
    <h2 style="color:#b00">Vous devez etre connecte</h2>
    <p>L'URL <b>${url}</b> necessite une session active.</p>
    <p><a href="<%= request.getContextPath() %>/login.jsp">Se connecter</a></p>
</body>
</html>
