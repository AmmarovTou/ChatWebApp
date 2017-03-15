/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WebSocket;

import Controllers.ServerController;
import Forms.MessageDTO;
import Forms.UserDTO;
import Model.Message;
import Model.MessageDAO;
import Model.User;
import Model.UserDAO;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import javax.websocket.server.ServerEndpoint;
import javax.enterprise.context.ApplicationScoped;

import javax.inject.Inject;

import java.io.StringReader;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author AMMAROV
 */
@ApplicationScoped
@ServerEndpoint("/chat")
public class ChatterWebSocketServer
{
    Integer connectedUserId = 0;
    
    @Inject
    private ChatSessionHandler connectedUsers;
    
    @OnOpen
    public void open(Session session)
    {
        System.out.println("web socket connection session with ID: "+session.getId()+", has been opened.");       
    }

    @OnClose
    public void close(Session session)
    {        
        Integer foundAt = getConnectedUserIndex(connectedUserId);
        if (foundAt != -1)
        {
            //connectedUsers.removeConnectedUser(foundAt);
            ConnectedUser connectedUserObj = connectedUsers.getConnectedUsers().get(foundAt);
            connectedUsers.removeConnectedUser(connectedUserObj);
        }
    }

    @OnError
    public void onError(Throwable error)
    {
        System.out.println("error thrown in web socket server: "+error.toString());
        Logger.getLogger(ChatterWebSocketServer.class.getName()).log(Level.SEVERE, null, error);
    }

    @OnMessage
    public void handleMessage(String message, Session session)
    {
        try (JsonReader reader = Json.createReader(new StringReader(message)))
        {
            JsonObject jsonMessage = reader.readObject();

            if("someOneIsTyping".equals(jsonMessage.getString("action")))
            {
                processSomeOneIsTyping(jsonMessage);
            }
            if ("sendMessage".equals(jsonMessage.getString("action")))
            {
                processSendMessage(jsonMessage, session);
            }
            
            if ("setConnectedUserId".equals(jsonMessage.getString("action")))
            {                
                processSetConnectedUserId(jsonMessage, session);
            }
            if ("getSenders".equals(jsonMessage.getString("action")))
            {                
                processGetSenders(jsonMessage);
            }         
            if ("markMsgsAsRead".equals(jsonMessage.getString("action")))
            {                
                processMarkMsgsAsRead(jsonMessage);
            }
            //a new user has been added by the admin
            if ("addNewUser".equals(jsonMessage.getString("action")))
            {                
                processAddNewUser(jsonMessage);
            }
            //a user has been deleted by the admin
            if ("removeUser".equals(jsonMessage.getString("action")))
            {                
                processRemoveUser(jsonMessage);
            }
        }//end of incoming JSON messages processor
    }
    
    private void processSomeOneIsTyping(JsonObject jsonMessage)
    {
        Integer senderId = jsonMessage.getInt("fromId");
        Integer toId = jsonMessage.getInt("toId");
        String senderName = jsonMessage.getString("senderName");

        Integer toFoundAt = getConnectedUserIndex(toId);

        if(toFoundAt != -1)
        {                    
            UserDTO userForm = new UserDTO(senderId, senderName);
            ConnectedUser connectedUserObj = connectedUsers.getConnectedUsers().get(toFoundAt);
            connectedUsers.sendSomeoneIsWrinting(connectedUserObj, userForm);
        }
    }
    
    private void processSendMessage(JsonObject jsonMessage, Session session)
    {
        String sentBy = ServerController.getUserNameFromDB(connectedUserId);
        Integer fromId = Integer.parseInt(jsonMessage.getString("fromId"));
        Integer toId = Integer.parseInt(jsonMessage.getString("toId"));
        String messageText = jsonMessage.getString("messageText");

        //if String for date is being used
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        GregorianCalendar calendar = new GregorianCalendar();
        String dateStr = format.format(calendar.getTime());
        String messageDate = dateStr;

        Integer readState = 0;

        MessageDTO messageForm = new MessageDTO(sentBy, fromId, toId, messageText, messageDate, readState);
        Message messageObj = new Message(fromId, toId, messageText, messageDate, readState);

        System.out.println("now message from client params: " + messageForm.toString());
        System.out.println("through web socket session ID: " + session.getId());
        ServerController.insertMsgIntoDb(messageObj);

        /*
        //for single tab only logic
        //check if the destination user is connected to server(online). if so, then send the message on socket
        Integer foundAt = getConnectedUserIndex(toId);
        if(foundAt != -1)
        {                    
            ConnectedUser connectedUserObj = connectedUsers.getConnectedUsers().get(foundAt);
            connectedUsers.sendMessage(connectedUserObj, messageForm);
        }
        */
        for (ConnectedUser connectedUserObj : connectedUsers.getConnectedUsers())
        {
            if (connectedUserObj.getUserId() == toId)
            {
                connectedUsers.sendMessage(connectedUserObj, messageForm);
            }
        }
        connectedUsers.sendAppendMessageNotif(fromId, messageForm);
    }
    
    private void processSetConnectedUserId(JsonObject jsonMessage, Session session)
    {
        Integer receivedId = Integer.parseInt(jsonMessage.getString("fromId"));
        connectedUserId = receivedId;

        /*
        //single web socket logic, thus single browser tab
         //to prevent duplicate entries and use tha latest provided session connection
         //loop over ids, if id already exist in list, get the index
         //then remove the ConnectedUser object from the list
         Integer foundAt = getConnectedUserIndex(connectedUserId);
         if(foundAt != -1)
         {
         //connectedUsers.removeConnectedUser(foundAt);
         ConnectedUser connectedUserObj = connectedUsers.getConnectedUsers().get(foundAt);
         connectedUsers.removeConnectedUser(connectedUserObj);
         }
         */
        connectedUsers.addConnectedUser(new ConnectedUser(session, receivedId));
        System.out.println("a new ConnectedUser object has been added.");
        for (ConnectedUser connectedUserObj : connectedUsers.getConnectedUsers())
        {
            System.out.println("web socket session: " + connectedUserObj.getSession().getId()
                    + ", user Id: " + connectedUserObj.getUserId());
        }
    }
    
    private void processGetSenders(JsonObject jsonMessage)
    {
        Integer unreadMsgsToUserId = Integer.parseInt(jsonMessage.getString("unreadMsgsToUserId"));
        Connection currentConnection = ServerController.getCurrentConnection();
        System.out.println("should get messages unread by userID: " + unreadMsgsToUserId);
        if(unreadMsgsToUserId != null)
        {
            if(currentConnection != null)
            {                        
                //System.out.println("next will try to fetch msgs from: "+currentUser.getId()+", to:"+toId);
                ArrayList<UserDTO> listOfSenders = MessageDAO.getListOfSenders(currentConnection, unreadMsgsToUserId);


                //check if the destination user is connected to server(online). if so, then send the message on socket
                Integer foundAt = getConnectedUserIndex(unreadMsgsToUserId);
                if (foundAt != -1)
                {
                    ConnectedUser connectedUserObj = connectedUsers.getConnectedUsers().get(foundAt);
                    connectedUsers.sendJArrayTo(connectedUserObj, listOfSenders);
                }
            }
        }
    }
    
    private void processMarkMsgsAsRead(JsonObject jsonMessage)
    {
        Integer msgsFromUserId = jsonMessage.getInt("msgsFromUserId");
        Integer msgsToUserId = jsonMessage.getInt("msgsToUserId");
        Connection currentConnection = ServerController.getCurrentConnection();
        if (currentConnection != null)
        {
            MessageDAO.markMsgsAsRead(currentConnection, msgsFromUserId, msgsToUserId);
            System.out.println("just called markMsgsAsRead with params, msgsFromUserId:" + msgsFromUserId
                    + ", msgsToUserId: " + msgsToUserId);
        }
    }
    
    private void processAddNewUser(JsonObject jsonMessage)
    {
        Integer initiatedById = jsonMessage.getInt("initiatedById");
        String userName = jsonMessage.getString("userName");
        String password = jsonMessage.getString("password");
        Integer isAdminCode = jsonMessage.getInt("isAdminCode");

        Connection currentConnection = ServerController.getCurrentConnection();
        if (currentConnection != null)
        {
            Boolean isAdmin = UserDAO.checkIfAdmin(currentConnection, initiatedById);
            if (isAdmin)
            {
                System.out.println("add user request initiated by userId: " + initiatedById + " is verfied as it's coming from an admin");
                Integer foundAt = getConnectedUserIndex(initiatedById);
                if (foundAt != -1)
                {
                    User userObj = new User(userName, password, isAdminCode);
                    Integer resultCode = UserDAO.insertUser(currentConnection, userObj);
                    System.out.println("result code of trying to insert the user: " + userObj.toString() + ", is: " + resultCode);
                    if (resultCode != -1)//user has been inerted successfully
                    {
                        Integer userId = UserDAO.getUserIdWithName(currentConnection, userName);
                        UserDTO userForm = new UserDTO(userId, userName);
                        System.out.println("new user DTO with conents, userId: " + userForm.getUserId()
                                + ", userName: " + userForm.getUserName());
                        connectedUsers.sendNewUserToAll(userForm, initiatedById);

                        //httpSession related update
                        ServerController.addToListOfUsers(userForm);
                    } 
                    else if (resultCode == -1)
                    {
                        connectedUsers.sendEarrorMsgToAdmin(initiatedById, userName);
                    }
                } 
                else
                {
                    System.out.println("user with id: " + initiatedById + ", is not connected anymore to the webSocket.");
                }
            } 
            else
            {
                System.out.println("request initiated by userId: " + initiatedById + " is not verfied as it's coming from an admin");
            }
        } 
        else
        {
            System.out.println("no available connection do DB");
        }
    }
    
    private void processRemoveUser(JsonObject jsonMessage)
    {
        Integer initiatedById = jsonMessage.getInt("initiatedById");
        Integer userId = jsonMessage.getInt("userId");

        Connection currentConnection = ServerController.getCurrentConnection();
        if (currentConnection != null)
        {
            Boolean isAdmin = UserDAO.checkIfAdmin(currentConnection, initiatedById);
            if (isAdmin)
            {
                System.out.println("remove user request initiated by userId: " + initiatedById + " is verfied as it's coming from an admin");
                Integer foundAt = getConnectedUserIndex(initiatedById);
                if (foundAt != -1)
                {
                    Integer resultCode = UserDAO.deleteUser(currentConnection, userId);
                    System.out.println("result code of trying to delete the user with id: " + userId + ", is: " + resultCode);
                    if (resultCode > -1)//user has been deleted successfully
                    {
                        connectedUsers.sendUserDeletedToAll(userId, initiatedById);

                        //httpSession related update
                        String userName = UserDAO.getUserNameWithId(currentConnection, userId);
                        UserDTO userForm = new UserDTO(userId, userName);
                        ServerController.deleteFromListOfUsers(userForm);
                    }
                }
                else
                {
                    System.out.println("user with id: " + userId + ", is not connected anymore to the webSocket.");
                }
            } 
            else
            {
                System.out.println("request initiated by userId: " + initiatedById + " is not verfied as it's coming from an admin");
            }
        } 
        else
        {
            System.out.println("no available connection do DB");
        }
    }
    
    public Integer getConnectedUserIndex(Integer connectedUserId)
    {        
        Integer foundAt = -1;
        for(int index = 0 ; index < connectedUsers.getConnectedUsers().size() ; index++)
        {
            //System.out.println("at index: "+index+", userId: "+connectedUsers.getConnectedUsers().get(index).getUserId());
            if(connectedUserId == connectedUsers.getConnectedUsers().get(index).getUserId())
            {
                foundAt = index;
                //System.out.println("now foundAt value: "+foundAt);
                return foundAt;
            }
        }
        return foundAt;
    }
}
