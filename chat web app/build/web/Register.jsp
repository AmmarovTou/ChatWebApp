<%-- 
    Document   : Register
    Author     : AMMAROV
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Register Page</title>
    </head>
    <body>
        <form action="AccountController" method="POST"> 
            
            <input type="hidden" name="source" value="Register"/>
            
            Username <input type="text" name="userName" required/><br> 
            Password <input type="text" name="password" required/><br> 
            
            <input type="submit" value="Register">
            
        </form>
    </body>
</html>
