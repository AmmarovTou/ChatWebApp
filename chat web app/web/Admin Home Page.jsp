<%-- 
    Document   : Admin Home Page
    Author     : AMMAROV
--%>

<%@page import="java.util.ArrayList"%>
<%@page import="java.sql.Connection"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%--
<%@page session="false" %>
--%>
<%@page import="Model.User" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Admin Home Page</title>
        <script src="https://code.jquery.com/jquery-1.10.2.js"></script>
        <script src="websocket.js"></script>
    </head>
    <body>               
        <%
            User currentUser = (User) session.getAttribute("currentSessionUser");
            if (null == currentUser)
            {
                response.sendRedirect("LogIn.jsp");
            }
            else
            {
                if(currentUser.getIsAdmin() == 0)
                {
                    String lastActivePage = (String) session.getAttribute("lastActivePage");
                    response.sendRedirect(lastActivePage);
                }
                else
                {
                    out.print("Welcome ");
                    out.println(currentUser.getUserName());
                    out.println("<br>");
                    out.println("Session ID: "+session.getId());
                    out.println("<br>");                
                    Connection connection = (Connection) session.getAttribute("currentConnection");
                    out.println("using connection: "+connection);
                    out.println("<br>");
                }
            }
        %> 
        
        <script>
            var selectedListOfUsers = new Array();
        </script>
        
        <form id="adminForm" name="adminForm">
            
            <input type="hidden" id="fromId" name="fromId">
            <input type="hidden" id="selectedListOfUsers" name="selectedListOfUsers">
            
            <script>
                var fromId = "";
                var jqxhr = $.post('AccountController', {getLoggedInUserId:"loggedInUserId"}, function(data)
                {
                    fromId = data;
                    $("#fromId").val(fromId);
                    alert($("#fromId").val());
                })
                  .done(function() {
                    sendToSocket();
                    //getSenders();
                  });

                
                function sendToSocket()
                {
                    var form = document.getElementById("adminForm");
                    var fromId = form.elements["fromId"].value;

                    var message = {
                        action: "setConnectedUserId",
                        fromId: fromId
                    };
                    alert("will try to send JSON with action: setConnectedUserId, fromId: " + fromId);
                    socket.send(JSON.stringify(message));
                }
            </script>
        </form>        
        
        <form action="AccountController" Id="addNewUserForm" method="POST"> 
            
            <input type="hidden" name="source" value="AddNewUserByAdmin"/>
            
            Username <input type="text" id="userName"  name="userName" required/>
            <br> 
            
            Password <input type="text" id="password" name="password" required/>
            <br>
            
            <input type="checkbox" id="isAdmin" name="isAdmin" value="isAdmin">Admin
            <br>                                    
            <input type="button" value="Add New User" onclick="AddNewUserByAdminSocket()">
        </form>
        
        <form action="AccountController" method="POST"> 
            <input type="hidden" name="source" value="Logout"/>
            <input type="submit" value="Logout">
        </form>               
        
        <select name="users" id="usersList" >
            <c:forEach items="${listOfUsers}" var="user">
                <option value="${user.getId()}">${user.getUserName()}</option>                     
            </c:forEach>
        </select>
        
        <form action="AccountController" method="POST">             
            <input type="button" name="source" value="Delete last selected user by admin" onclick="removeUserThroughSocket(selectedListOfUsers)">
            <script>
                
            function printSelectedIds()
            {
                if(selectedListOfUsers.length > 0)
                {
                    $.post('AccountController', {usersToDel:selectedListOfUsers, mode:"update"}, function(data)
                    {
                        alert("Data: " + data);
                    });
                }
            }
            </script>
        </form>        
        
        <script>
            
        $( "#usersList" ).change(function () 
        {
            var selectedUsers = "";
            //var selectedListOfUsers = new Array();
            selectedListOfUsers = new Array();
            $( "select option:selected" ).each(function() 
            {
                selectedListOfUsers.push($( this ).val());
                selectedUsers += $( this ).val() +":"+ $( this ).text() + ",";
            });
            $( "#selectionDiv" ).text( selectedUsers );
        }).change();
        </script>
        
        <script>
            function AddNewUserByAdmin()
            {
                var form = document.getElementById("addNewUserForm");
                var userName = form.elements["userName"].value;
                var password = form.elements["password"].value;                    

                var isAdminCode = 0;
                if ($('#isAdmin').is(":checked"))
                {
                    isAdminCode = 1;
                }
                //alert("userName: "+userName+", password: "+password+", isAdminChk: "+isAdminCode);
                
                var jqxhr = $.post('AccountController',
                {addNewUser:"addNewUser",
                    initiatedById: fromId,
                    userName: userName,
                    password: password,
                    isAdminCode: isAdminCode},
                function(data)
                {
                    alert("userName: "+userName+", password: "+password+", isAdminChk: "+isAdminCode);
                })
                  .done(function() {alert("alert-insert successfull");});                  
            }
        </script>
        
        <div id="demoDiv"></div>
        <div id="listOfUsersDiv"></div>               
        <div id="lastSelectedDiv"></div> 
        <div id="selectionDiv"></div>
        
    </body>
</html>
