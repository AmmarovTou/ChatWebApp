<%-- 
    Document   : User Home Page
    Author     : AMMAROV
--%>

<%@page import="java.util.ArrayList"%>
<%@page import="java.sql.Connection"%>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%--
<%@page session="false" %>
--%>
<%@page import="Model.User" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>User Home Page</title>
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
                if(currentUser.getIsAdmin() == 1)
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
        
        <form action="AccountController" method="POST"> 
            <input type="hidden" name="source" value="Logout"/>
            <input type="submit" value="Logout">
        </form>               
                      
        <form id="chattingForm" action="AccountController" method="POST">             
            
            <input type="hidden" id="selectedListOfUsers" name="selectedListOfUsers">
            <input type="hidden" id="loggedInUserName" name="loggedInUserName">
            <input type="hidden" id="fromId" name="fromId">
            
            <!--show list of users to view incoming/outcoming messages from the selected user-->
            <select name="users" id="usersList" multiple="multiple">
                <c:forEach items="${listOfUsers}" var="user">
                    <option value="${user.getId()}" >${user.getUserName()}</option>                     
                </c:forEach>
            </select>  
            <br>
            
            <!--incoming and outcoming messages shown here:-->
            <textarea class="scrollabletextbox" name="messagesBox" id="messagesBox" style="width: 200px;height: 400px;">
            </textarea>
            <br>
            
            <!--new message send through this text box:-->
            <input type="text" id="messageText" name="messageText" onkeyup="userIsTyping()">
            <br>            
            
            <!--submit form for sending message processing, sent thorugh web socket-->
            <input type="button" name="source" value="send message" onclick="formSubmit()">
            <br> 
            
            <!--chattingForm script-->
            <script>
                var selectedListOfUsers = new Array(); 
                var fromId = "";
                
                var userIdReq = $.post('AccountController', {getLoggedInUserId:"loggedInUserId"}, function(data)
                {
                    fromId = data;
                    $("#fromId").val(fromId);
                    alert($("#fromId").val());
                })
                  .done(function() {
                    sendToSocket();
                    getSendersThroughSocket();
                  });

                
                function sendToSocket()
                {
                    var form = document.getElementById("chattingForm");
                    var fromId = form.elements["fromId"].value;

                    var message = {
                        action: "setConnectedUserId",
                        fromId: fromId
                    };
                    alert("will try to send JSON with action: setConnectedUserId, fromId: " + fromId);
                    socket.send(JSON.stringify(message));
                }

                
                var loggedInUserName = "";
                var userNameReq = $.post('AccountController', {getLoggedInUserName:"loggedInUserName"}, function(data)
                {
                    loggedInUserName = data;
                    $("#loggedInUserName").val(loggedInUserName);                    
                })
                  .done(function() {
                      //alert($("#loggedInUserName").val());
                  });  
                
                  
                function userIsTyping()
                {
                    //$("#textChangedDiv").text($("#messageText").val());
                    //sendSomeOneIsTyping();
                }
                
                
            </script>   
            <!--chattingForm script ends here-->
            
        </form>                         
        
        <!--listener on the list of users view, do track which users are being selected
        to send/recieve messages to/from the selected users
        -->
        <script>
        $( "#usersList" ).change(function () 
        {
            var selectedUsers = "";
            //var selectedListOfUsers = new Array();
            selectedListOfUsers = new Array();
            $( "select option:selected" ).each(function() 
            {
                selectedListOfUsers.push($( this ).val());
                $("#selectedListOfUsers").val(selectedListOfUsers);
                selectedUsers += $( this ).val() +":"+ $( this ).text() + ",";
                
                if($(this).css("font-weight") === "bold")
                {                    
                    $(this).css("font-weight","normal");
                    markMsgsAsRead();
                }
                var cssCode = parseInt($(this).css("font-weight"));
                if(cssCode === 700)
                {
                    $(this).css("font-weight","normal");
                    markMsgsAsRead();
                }
            });
            $( "#selectionDiv" ).text( selectedUsers );
            clearMessagesBox();
            
            if(selectedListOfUsers.length > 0)
            {
                var lastSelectedUser = $(selectedListOfUsers).last()[0];
                getMessagesBtween(lastSelectedUser);
            }
        }).change();
        </script>
        <!--list of users view listener script ends here-->
        
        <script>
            //fired when the message sent
            function clearMessagesBox()
            {
                $("#messagesBox").val("");
            }
            
            //fired when the user clicks on an item from the list of users view
            function getMessagesBtween(toId)
            {
                $( "#lastSelectedDiv" ).text( "last selected user Id: "+toId );
                var msgBoxVal = "";
                $.post('AccountController', {getMessages:toId, mode:"update", fromId:toId}, function(data)
                {
                    msgBoxVal += data;
                    $("#messagesBox").val(msgBoxVal);
                });
                //alert(msgBoxVal);
            }
        </script>
       
        <div id="selectionDiv"></div>
        <div id="demoDiv"></div>
        <div id="listOfUsersDiv"></div>    
        <div id="lastSelectedDiv"></div>
        <div id="textChangedDiv"></div>
    </body>
</html>
