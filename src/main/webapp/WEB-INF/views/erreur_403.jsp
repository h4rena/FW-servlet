<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<html>
<head><title>Acces refuse - role insuffisant</title></head>
<body>
    <h2 style="color:#b00">Acces refuse</h2>
    <p>L'URL <b>${url}</b> n'est pas accessible avec votre role.</p>
    <p>Role requis : <b>${roleRequis}</b></p>
    <p>Votre role : <b>${roleActuel}</b></p>
    <p><a href="<%= request.getContextPath() %>/login.jsp">Changer d'utilisateur</a></p>
</body>
</html>
