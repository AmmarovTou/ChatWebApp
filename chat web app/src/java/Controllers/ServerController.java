/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controllers;

import Forms.UserDTO;
import Model.CacheInMemory;
import Model.ConnectionPoolManager;
import Model.Message;
import Model.MessageDAO;
import Model.User;
import Model.UserDAO;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.JsonObject;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.json.*;
import javax.json.spi.JsonProvider;

/**
 *
 * @author AMMAROV
 */
public class ServerController extends HttpServlet
{
    static HttpSession session;
    
    //was static
    ConnectionPoolManager connectionPool = new ConnectionPoolManager();    
    
    static Connection currentStaticConnection;
    
    public static Boolean shouldUpdateUsersCache = true;
    public static ArrayList<String> listOfFetchedClientsIds = new ArrayList<String>();
    public static CacheInMemory<String, String> usersCache = new CacheInMemory<String, String>(30);//LRUmap of size 30
    
    static Integer currentUserId = 0;
        
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        PrintWriter out = response.getWriter();               
    }
    
    public static void insertMsgIntoDb(Message messageObj)
    {
        if (currentStaticConnection != null)
        {
            MessageDAO.insertMessage(messageObj, currentStaticConnection);
        }
    }
    public static String getUserNameFromDB(Integer userId)
    {
        String userName = null;
        if(currentStaticConnection != null)
        {
            userName = UserDAO.getUserNameWithId(currentStaticConnection, userId);
        }
        return userName;
    }
    public static Connection getCurrentConnection()
    {
        return currentStaticConnection;
    }
    public static void addToListOfUsers(UserDTO userForm)
    {        
        listOfFetchedClientsIds.add(userForm.getUserId()+"");
        ArrayList<User> listOfUsers = (ArrayList<User>) session.getAttribute("listOfUsers");
        if(listOfUsers != null)
        {
            listOfUsers.add(new User(userForm.getUserId(), userForm.getUserName()));
            session.removeAttribute("listOfUsers");
            session.setAttribute("listOfUsers", listOfUsers);
        }
    }
    public static void deleteFromListOfUsers(UserDTO deletedUser)
    {
        ArrayList<User> listOfUsers = (ArrayList<User>) session.getAttribute("listOfUsers");
        if(listOfUsers != null)
        {
            Integer delAtIndex = -1;
            for(Integer index = 0 ; index < listOfUsers.size() ; index++)
            {
                if(deletedUser.getUserId() == listOfUsers.get(index).getId())
                {
                    delAtIndex = index;
                }
            }
            if(delAtIndex != -1)
            {                
                User dummyUser = listOfUsers.get(delAtIndex);
                listOfUsers.remove(dummyUser);
                listOfFetchedClientsIds.remove(deletedUser.getUserId()+"");
                /*
                System.out.println("deleted user with ID: "+deletedUser.getUserId()+" has been removed from the list at index: "+delAtIndex+", now list conents: ");
                for (User userObj : listOfUsers)
                {
                    System.out.println(userObj.getId()+" : "+userObj.getUserName());
                }
                System.out.println("listOfFetchedClientsIds contents: ");
                for (String strId : listOfFetchedClientsIds)
                {
                    System.out.println(strId);
                }
                */
                session.removeAttribute("listOfUsers");
                session.setAttribute("listOfUsers", listOfUsers);
            }
        }
    }

    
    protected Boolean processAjaxRequests(HttpServletRequest request, HttpServletResponse response, Enumeration<String> paramNames, PrintWriter out, Boolean ajaxProcessed)
    {
        //Boolean ajaxProcessed = false;
        String param = paramNames.nextElement();
        if (param.equals("getLoggedInUserId"))
        {
            System.out.print(param);

            User currentUser = (User) session.getAttribute("currentSessionUser");
            if (currentUser != null)
            {
                out.print(currentUser.getId());
                System.out.println("just sent to client: fromId:" + currentUser.getId());
            }

            ajaxProcessed = true;
        } 
        else if (param.equals("getLoggedInUserName"))
        {
            System.out.print(param);

            User currentUser = (User) session.getAttribute("currentSessionUser");
            if (currentUser != null)
            {
                out.print(currentUser.getUserName());
                System.out.println("just sent to client: userName:" + currentUser.getUserName());
            }
            ajaxProcessed = true;
        } 
        else if (param.equals("registerUser"))
        {
            System.out.print(param);

            paramNames.nextElement();
            String userName = paramNames.nextElement();
            out.print(userName);
            System.out.println("next param: " + userName);

            ajaxProcessed = true;
        } 
        else if (param.equals("usersToDel[]"))
        {
            System.out.print(param);

            String[] usersToDel = request.getParameterValues("usersToDel[]");
            for (String userId : usersToDel)
            {
                out.print(userId + ", ");
                System.out.println("userId to be deleted: " + userId);
                //ajaxProcessed = true;
            }
            ajaxProcessed = true;
        } 
        else if (param.equals("mode"))
        {
            System.out.println("mode: " + request.getParameter("mode"));
            ajaxProcessed = true;
        } 
        else if (param.equals("getMessages"))
        {
            System.out.print(param);

            String messageToUserId = request.getParameter("getMessages");
            System.out.println("should get all messages from user with ID: " + messageToUserId);
            if (messageToUserId != null)
            {
                User currentUser = (User) session.getAttribute("currentSessionUser");
                Connection currentConnection = (Connection) session.getAttribute("currentConnection");

                if (currentUser != null && currentConnection != null)
                {
                    Integer toId = Integer.parseInt(messageToUserId);
                    System.out.println("next will try to fetch msgs from: " + currentUser.getId() + ", to:" + toId);
                    ArrayList<Message> listOfMessages = MessageDAO.getMessagesBetween(toId, currentUser.getId(), currentConnection);
                    for (Message messageObj : listOfMessages)
                    {
                        out.println(messageObj.getMessageText());
                        /*out.println("fromId: "+messageObj.getFromId()
                         +", toId: "+messageObj.getToId()
                         +", message text: "+messageObj.getMessageText());*/
                    }
                }
            }
            ajaxProcessed = true;
        } 
        else if (param.equals("getSenders"))
        {
            System.out.print(param);

            String unreadMsgsToUserId = request.getParameter("getSenders");
            System.out.println("should get messages unread by userID: " + unreadMsgsToUserId);
            if (unreadMsgsToUserId != null)
            {
                User currentUser = (User) session.getAttribute("currentSessionUser");
                Connection currentConnection = (Connection) session.getAttribute("currentConnection");

                if (currentUser != null && currentConnection != null)
                {
                    Integer toId = Integer.parseInt(unreadMsgsToUserId);
                    //System.out.println("next will try to fetch msgs from: "+currentUser.getId()+", to:"+toId);
                    ArrayList<UserDTO> listOfSenders = MessageDAO.getListOfSenders(currentConnection, toId);

                    JsonProvider provider = JsonProvider.provider();
                    JsonArrayBuilder jArrayBulider = provider.createArrayBuilder();
                    JsonArray jsonSendersArray;

                    for (UserDTO senderUserDTO : listOfSenders)
                    {

                        JsonObject senderJsonObject = provider.createObjectBuilder()
                                .add("fromUserId", senderUserDTO.getUserId())
                                .add("senderName", senderUserDTO.getUserName())
                                .build();

                        jArrayBulider = jArrayBulider.add(senderJsonObject);
                    }
                    jsonSendersArray = jArrayBulider.build();
                    if (jsonSendersArray.size() > 0)
                    {
                        System.out.println("jsonSendersArray size: " + jsonSendersArray.size() + ", with contents: ");
                        for (int index = 0; index < jsonSendersArray.size(); index++)
                        {
                            System.out.println(jsonSendersArray.getJsonObject(index).getJsonNumber("fromUserId")
                                    + " : "
                                    + jsonSendersArray.getJsonObject(index).getJsonString("senderName"));
                        }
                    }
                    out.print(jsonSendersArray);
                }
            }
            ajaxProcessed = true;
        }
        /*
         else if(param.equals("addNewUser"))
         {
         System.out.print(param);
                    
         String unreadMsgsToUserId = request.getParameter("getSenders");
         System.out.println("should get messages unread by userID: " + unreadMsgsToUserId);
         if(unreadMsgsToUserId != null)
         {}
                    
         ajaxProcessed = true;
         }
         */
        return ajaxProcessed;
    }
    
    protected void processHttpPostRequest(HttpServletRequest request, HttpServletResponse response, PrintWriter out)
            throws ServletException, IOException
    {
        if (request.getParameter("source").equals("Login"))
        {
            /*
             User userObj = new User();
             userObj.setUserName(request.getParameter("userName"));
             userObj.setPassword(request.getParameter("password"));
             */

                //last used
            //session = request.getSession(true);

            /*
             if (session == null)
             {
             session = request.getSession();
             }
             */
            User loggedInUser = null;
            loggedInUser = (User) session.getAttribute("currentSessionUser");

            if (loggedInUser != null)
            {
                String userNameFromRequest = request.getParameter("userName");
                if (loggedInUser.getUserName().equals(userNameFromRequest))
                {
                    out.println("<script type=\"text/javascript\">");
                    out.println("alert('account with Username: " + userNameFromRequest
                            + ", already logged in, please log in with a different account.');");
                    out.println("location='LogIn.jsp';");
                    out.println("</script>");

                    //added to testing
                    session.setAttribute("currentSessionUser", loggedInUser);
                } 
                else
                {
                    //doNewUserLoginProcessing(request, response, userObj);
                    doNewUserLoginProcessing(request, response);
                }
            }
            else
            {
                //doNewUserLoginProcessing(request, response, userObj);
                doNewUserLoginProcessing(request, response);
            }
        }
        else if (request.getParameter("source").equals("Register"))
        {
            out.println("user should be registered with the following credentials,"
                    + "User name: " + request.getParameter("userName")
                    + ", and password: " + request.getParameter("password"));
        } else if (request.getParameter("source").equals("AddNewUserByAdmin"))
        {
            User userObj = new User();
            userObj.setUserName(request.getParameter("userName"));
            userObj.setPassword(request.getParameter("password"));

            String[] isAdminChkBoxes = request.getParameterValues("isAdmin");
            if (isAdminChkBoxes != null)
            {
                for (String checkBoxValue : isAdminChkBoxes)
                {
                    //out.println("is registered as admin? " + checkBoxValue);
                    userObj.setIsAdmin(1);
                }
            }
            else
            {
                //out.println("is registered as admin? Nah");
                userObj.setIsAdmin(0);
            }

            Connection connection = (Connection) session.getAttribute("currentConnection");

            Integer resultCode = UserDAO.insertUser(connection, userObj);
            if (resultCode > 0)
            {
                out.println("<script type=\"text/javascript\">");

                out.println("alert('a new user was registered with the following credentials,"
                        + "User name: " + userObj.getUserName() + ", and password: " + userObj.getPassword()
                        + ", is registered as admin? " + userObj.getIsAdmin()
                        + ", using connection ID: " + connection.toString() + "');");

                out.println("location='Admin Home Page.jsp';");

                out.println("</script>");
            }
            else if (resultCode < 0)
            {
                out.println("<script type=\"text/javascript\">");
                out.println("alert('Username: " + userObj.getUserName() + ", already exists in the database, please try another one.');");
                out.println("location='Admin Home Page.jsp';");
                out.println("</script>");
            }
        } 
        else if (request.getParameter("source").equals("Delete user by admin"))
        {
            /*
             System.out.println("now getting a request via jquery.");
             Enumeration<String> paramNames = request.getParameterNames();
             while (paramNames.hasMoreElements())
             {
             System.out.print(paramNames.nextElement().toString());
             }
             String[] usersToDel = request.getParameterValues("arrayData[]");
             for (String userId : usersToDel)
             {
             System.out.println("userId to be deleted: " + userId);
             }
             */
        } 
        else if (request.getParameter("source").equals("Logout"))
        {
            closeSession(request);

            /*
             RequestDispatcher disp = request.getRequestDispatcher("LogIn.jsp");                    
             disp.forward(request, response);
             */
            response.sendRedirect("LogIn.jsp");
        }
    }
    
    //protected void doNewUserLoginProcessing(HttpServletRequest request, HttpServletResponse response, User userObj)
    protected void doNewUserLoginProcessing(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
   {             
       //HttpSession session = request.getSession();
       
        PrintWriter out = response.getWriter();
        
        //userObj should be turned into an object of UserDTO
        User userObj = new User();
        userObj.setUserName(request.getParameter("userName"));
        userObj.setPassword(request.getParameter("password"));
        System.out.println("request contains userName: "+userObj.getUserName()+", and password: "+userObj.getPassword());
        try
        {
            RequestDispatcher disp;

            Connection currentConnection = connectionPool.getConnectionFromPool();
            if(currentConnection != null)
            {
                userObj = UserDAO.logIn(currentConnection, userObj);
                System.out.println("trying to log in.");
                String lastActivePage = "";
                if (userObj.getIsValid())
                {
                    currentUserId++;

                    //session = request.getSession(true);
                    session.setAttribute("currentSessionUser", userObj);

                    currentStaticConnection = currentConnection;
                    session.setAttribute("currentConnection", currentConnection);                    
                    session.setMaxInactiveInterval(4*60); //4 minutes 

                    if(userObj.getIsAdmin() == 1)
                    {
                        session.setAttribute("lastActivePage", "Admin Home Page.jsp");
                        lastActivePage = "Admin Home Page.jsp";
                        disp = request.getRequestDispatcher("Admin Home Page.jsp");
                        System.out.println("logged in with admin privileges.");
                    }
                    else
                    {
                        session.setAttribute("lastActivePage", "User Home Page.jsp");
                        lastActivePage = "User Home Page.jsp";
                        disp = request.getRequestDispatcher("User Home Page.jsp");
                        System.out.println("logged in with user privileges.");
                    }
                    
                    
                    ArrayList<User> listOfUsers;
                    if(shouldUpdateUsersCache)
                    {
                        System.out.println("server is updating and re-filling the users cache");                        
                        
                        listOfUsers = new ArrayList();
                        listOfFetchedClientsIds = new ArrayList<String>();
                        
                        //resolved bug: this was being called b4 the connection is aquired by the loggedin user
                        listOfUsers = UserDAO.returnAllUsers(currentConnection, userObj.getUserName());
                        
                        
                        listOfFetchedClientsIds.add(userObj.getId()+"");
                        usersCache.put(userObj.getId() + "", userObj.getUserName()); //key and value pairs are defined as <String, String>
                        for (User fetchedUser : listOfUsers)
                        {
                            listOfFetchedClientsIds.add(fetchedUser.getId()+"");
                            usersCache.put(fetchedUser.getId() + "", fetchedUser.getUserName()); //key and value pairs are defined as <String, String>
                        }
                        
                        session.setAttribute("listOfUsers", listOfUsers);
                        shouldUpdateUsersCache = false;
                    }
                    else
                    {
                        System.out.println("server is utilizing the users cache to return the set of users");
                        listOfUsers = new ArrayList();
                        
                        for (String fetchedUserId : listOfFetchedClientsIds)
                        {
                            Integer intUserId = Integer.parseInt(fetchedUserId);
                            if(intUserId != userObj.getId())
                            {
                                String userName = usersCache.get(fetchedUserId);
                                User fetchedUserObj = new User(intUserId, userName);
                                listOfUsers.add(fetchedUserObj);
                            }
                        }
                        session.setAttribute("listOfUsers", listOfUsers);
                    }                    
                    
                    //added
                    //disp.forward(request, response);
                    response.sendRedirect(lastActivePage);
                    //disp.include(request, response);
                }

                /*
                //should be used, if "//added" is removed
                //response.sendRedirect("User Home Page.jsp");
                if(userObj.getIsAdmin() == 1)
                    disp = request.getRequestDispatcher("Admin Home Page.jsp");
                else
                    disp = request.getRequestDispatcher("User Home Page.jsp");
                */
            }

            //failed to aquire a connection
            else
            {
                System.out.println("failed to acquire connection.");
                
                //response.sendRedirect("index.html");
                disp = request.getRequestDispatcher("index.html"); 
                
                //added
                disp.forward(request, response);
            }
            //should be used, if "//added" is removed
            //disp.forward(request, response);
        }
        catch (Exception exc)
        {
            Logger.getLogger(ServerController.class.getName()).log(Level.SEVERE, null, exc);
            //System.out.println(exc);
        }        
    }
    
    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        //HttpSession session = null;
        /*
        if(session == null)
        {
            session = request.getSession(false);
            System.out.println("doPost-getting a new session with ID: "+session.getId());
        }
        */
        session = request.getSession();
        System.out.println("doPost-getting a new session with ID: " + session.getId());

        PrintWriter out = response.getWriter();       
        
        Boolean ajaxProcessed = false;
        Enumeration<String> paramNames = request.getParameterNames();
        if(paramNames.hasMoreElements())
        {
            while (paramNames.hasMoreElements())
            {
                ajaxProcessed = processAjaxRequests(request, response, paramNames, out, ajaxProcessed);
            }                         
        }
        
        if(!ajaxProcessed)
        {
            processHttpPostRequest(request, response, out);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo()
    {
        return "Short description";
    }// </editor-fold>

    public void closeSession(HttpServletRequest request)
    {       
        if(session != null)
        {
                      
            Connection currentConnection = (Connection) session.getAttribute("currentConnection");
            if (currentConnection != null)
            {
                connectionPool.returnConnectionToPool(currentConnection);
            } 
            session.removeAttribute("currentSessionUser");
            try
            {
                if(session.isNew())
                    session.invalidate();
            }
            catch(Exception ex)
            {
                System.out.println("session is already invalidated");
            }
        }
    }
    
    /*
    @WebListener
    public class MyHttpSessionListener implements HttpSessionListener
    {
        @Override
        public void sessionCreated(HttpSessionEvent se)
        {
            
        }

        @Override
        public void sessionDestroyed(HttpSessionEvent se)
        {
            closeSession();
        }
    }
    */
}
