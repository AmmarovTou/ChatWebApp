<%-- 
    Document   : LogIn
    Author     : AMMAROV
--%>

<%@page import="java.sql.Connection"%>
<%@page import="Model.User"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%--
<%@page session="false" %>
--%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Login Page</title>
        
        <link rel='stylesheet prefetch' href='http://fonts.googleapis.com/css?family=Open+Sans:600'>
        <link rel="stylesheet" href="css/style.css">
        
        <script src="https://code.jquery.com/jquery-1.10.2.js"></script>
        <script src="websocket.js"></script>
        
    </head>
    <body>
        <%
            User currentUser = (User) session.getAttribute("currentSessionUser");
            if(null != currentUser)
            {
                String lastActivePage = (String) session.getAttribute("lastActivePage");
                if(lastActivePage != null)
                {
                    System.out.println("should redirect from login to "+lastActivePage);
                    response.sendRedirect(lastActivePage);
                }
            }            
        %> 
        
        
        <form id="logRegForm" action="AccountController" method="POST"> 
            
            <!--
            <input type="hidden" name="source" value="LogIn"/>
            
            Username <input type="text" name="userName"/><br> 
            Password <input type="text" name="password"/><br>
            You don't have an account? then <a href="Register.jsp">create one</a><br>
            <input type="submit" value="Login">
            -->
            
            <div class="login-wrap">
                <div class="login-html">
                    <input id="tab-1" type="radio" name="tab" class="sign-in" checked><label for="tab-1" class="tab">Login</label>                                        
                    <input id="tab-2" type="radio" name="tab" class="sign-up"><label for="tab-2" class="tab">Register</label>                  
                    
                    
                    <div class="login-form">
                        <div class="sign-in-htm">
                            <div class="group">
                                <label for="user" class="label">Username</label>
                                <input id="user" type="text" class="input" name="userName" required>
                            </div>
                            <div class="group">
                                <label for="pass" class="label">Password</label>
                                <input id="pass" type="password" class="input" data-type="password" name="password" required>
                            </div>                            
                            <div class="group">
                                <input type="submit" class="button" value="Login" name="source">
                            </div>                                                        
                        </div>
                        
                        
                        <!--
                        <div class="sign-up-htm">
                            <div class="group">
                                <label for="user" class="label">Username</label>
                                <input id="userReg" type="text" class="input" name="userName" >
                            </div>
                            <div class="group">
                                <label for="pass" class="label">Password</label>
                                <input id="passReg" type="password" class="input" data-type="password" name="password" >
                            </div>
                            <div class="group">
                                <input type="button" class="button" value="Register" name="source" onclick="registerUser()">
                            </div>                            
                            
                        </div>
                        -->
                    </div>
                </div>
            </div>
        </form>
    </body>
</html>
