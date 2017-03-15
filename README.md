# ChatWebApp
Realtime chat web application using J2EE and HTML5 web socket.
-It's a Java web application project. The goal was to create a realtime chatting web app.
-Built around the MVC design pattern, alongside other design patterns like DAO and DTO design patterns.
-Two login roles, either as an admin or as a user.
-The admin can add and delete a user.
-The user can chat with other users in realtime using HTML5 web socket
rather than using long pooling techniques to achieve realtime chatting.
-JSON was used for messages between the client and the server.
-A connection pool was used to provide a connection to the database for each new connected user.
-The list of available users is cached at the server to improve application's performance and reduce overhead on the database.
-A servlet has been used to represent the controller(the server), and JSPs for the views(web pages).
-And of course, Javascript, JQuery and AJAX have been used in this project.
