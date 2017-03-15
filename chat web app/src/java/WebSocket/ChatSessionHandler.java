/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WebSocket;

import Forms.MessageDTO;
import Forms.UserDTO;
import java.util.ArrayList;
import javax.enterprise.context.ApplicationScoped;

import javax.json.JsonObject;
import javax.websocket.Session;

import javax.json.spi.JsonProvider;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;

/**
 *
 * @author AMMAROV
 */
@ApplicationScoped
public class ChatSessionHandler
{
    static protected ArrayList<ConnectedUser> connectedUsersList = new ArrayList<>();
    
    public void addConnectedUser(ConnectedUser connectedUserObj)
    {
        connectedUsersList.add(connectedUserObj);
    }
    public ArrayList<ConnectedUser> getConnectedUsers()
    {
        return connectedUsersList;
    }
    
    
    //public void removeConnectedUser(Integer atIndex)
    public void removeConnectedUser(ConnectedUser connectedUser)
    {
        //connectedUsersList.remove(atIndex);
        connectedUsersList.remove(connectedUser);
        //System.out.println("a connectedUser obect has been removed from index: "+atIndex);
        System.out.println("a connectedUser obect has been removed.");
        System.out.println("new conents of list of connected users:");
        for (ConnectedUser connectedUserObj : connectedUsersList)
        {
            System.out.println("web socket session: "+connectedUserObj.getSession().getId()
                                        +", user Id: "+connectedUserObj.getUserId());
        }
        System.out.println("---------------");
    }
    public void sendSomeoneIsWrinting(ConnectedUser connectedUserObj, UserDTO userForm)
    {
        JsonObject jsonMessage = createWriterMsg(userForm);
        sendToSession(connectedUserObj.getSession(), jsonMessage);
    }
    public void sendMessage(ConnectedUser destinationUser, MessageDTO messageToBeSent)
    {
        JsonObject jsonMessage = createSendMessage(messageToBeSent);
        sendToSession(destinationUser.getSession(), jsonMessage);
    }
    //sends a notifier to the other opened tabs which hold the same sender id, to append the sent message
    public void sendAppendMessageNotif(Integer fromId, MessageDTO messageToBeSent)
    {
        JsonObject jsonMessage = createAppendNot(messageToBeSent);
        for (ConnectedUser connectedUserObj : connectedUsersList)
        {
            if(fromId == connectedUserObj.getUserId())
            {
                sendToSession(connectedUserObj.getSession(), jsonMessage);
            }
        }
    }
    public void sendJArrayTo(ConnectedUser destinationUser, ArrayList<UserDTO> listOfSenders)
    {
        JsonArray jsonSendersArray = createJsonArray(listOfSenders);
        sendJArrayToSession(destinationUser.getSession(), jsonSendersArray);
    }
    //a new user has been added successfull by an admin, send data to all connected peers
    public void sendNewUserToAll(UserDTO userForm, Integer initiator)
    {
        JsonObject newUserMsg = createNewUserMsg(userForm, initiator);
        for (ConnectedUser connectUserObj : connectedUsersList)
        {
            System.out.println("now sending a json response to userId: "+connectUserObj.getUserId()
                    +", message conents: "+newUserMsg);
            sendToSession(connectUserObj.getSession(), newUserMsg);
        }
    }
    public void sendEarrorMsgToAdmin(Integer sendToAdminId, String userName)
    {
        JsonObject newErrorMsgToAdmin = createFailedToAddMsg(sendToAdminId, userName);        
        for(Integer index = 0 ; index < connectedUsersList.size() ; index++)
        {
            if(connectedUsersList.get(index).getUserId() == sendToAdminId)
            {
                ConnectedUser adminObj = connectedUsersList.get(index);
                System.out.println("now sending a json response to userId: "+adminObj.getUserId()
                    +", message conents: "+newErrorMsgToAdmin);
                sendToSession(adminObj.getSession(), newErrorMsgToAdmin);
            }
        }        
    }
    public void sendUserDeletedToAll(Integer deletedUserId, Integer initiatedById)
    {
        JsonObject usrDeletedMsg = createUserDeletedMsg(deletedUserId, initiatedById);
        for (ConnectedUser connectUserObj : connectedUsersList)
        {
            System.out.println("now sending a json response to userId: "+connectUserObj.getUserId()
                    +", message conents: "+usrDeletedMsg);
            sendToSession(connectUserObj.getSession(), usrDeletedMsg);
        }
    }

    
    private JsonObject createWriterMsg(UserDTO userForm)
    {
        JsonProvider provider = JsonProvider.provider();
        JsonObject writerMsg = provider.createObjectBuilder()                
                .add("action", "someOneIsTyping")                    
                .add("fromId", userForm.getUserId())
                .add("senderName", userForm.getUserName())
                .build();        
        return writerMsg;
    }
    private JsonObject createSendMessage(MessageDTO messageObj)
    {
        JsonProvider provider = JsonProvider.provider();
        JsonObject sendMessage = provider.createObjectBuilder()                
                .add("action", "receiveMessage")    
                .add("sentBy", messageObj.getSentBy())
                .add("fromId", messageObj.getFromId())
                .add("toId", messageObj.getToId())
                .add("messageText", messageObj.getMessageText())
                .add("date", messageObj.getMessageDate())
                .add("readState", messageObj.getReadState())
                .build();        
        return sendMessage;
    }    
    private JsonObject createAppendNot(MessageDTO messageObj)
    {
        JsonProvider provider = JsonProvider.provider();
        JsonObject notifMessage = provider.createObjectBuilder()                
                .add("action", "appendSentMessage")    
                .add("sentBy", "me: ")
                .add("fromId", messageObj.getFromId())
                .add("toId", messageObj.getToId())
                .add("messageText", messageObj.getMessageText())
                .add("date", messageObj.getMessageDate())
                .add("readState", messageObj.getReadState())
                .build();        
        return notifMessage;
    }
    private JsonObject createNewUserMsg(UserDTO userForm, Integer initiator)
    {
        JsonProvider provider = JsonProvider.provider();
        JsonObject newUserJsonObj = provider.createObjectBuilder()                
                .add("action", "recieveNewUser")    
                .add("result", "User with userId:"+ userForm.getUserId() 
                        +", and userName: "+ userForm.getUserName()
                        +" has been successfully added to the database.")  
                .add("initiator", initiator)
                .add("userId", userForm.getUserId())
                .add("userName", userForm.getUserName())
                .build();        
        return newUserJsonObj;
    }
    private JsonObject createUserDeletedMsg(Integer deletedUserId, Integer initiatedById)
    {
        JsonProvider provider = JsonProvider.provider();
        JsonObject userDeletedJsonObj = provider.createObjectBuilder()                
                .add("action", "userDeleted")    
                .add("result", "User with userId:"+ deletedUserId
                        +" has been successfully deleted from the database.")  
                .add("initiator", initiatedById)
                .add("deletedUserId", deletedUserId)
                .build();        
        return userDeletedJsonObj;
    }
    private JsonArray createJsonArray(ArrayList<UserDTO> listOfSenders)
    {
        JsonProvider provider = JsonProvider.provider();
        JsonArrayBuilder jArrayBulider = provider.createArrayBuilder();
        JsonArray jsonSendersArray;
        
        JsonObject actionObject = provider.createObjectBuilder()
                    .add("action", "processSenders")                    
                    .build();
        jArrayBulider = jArrayBulider.add(actionObject);
        
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
            System.out.println("action : "+jsonSendersArray.getJsonObject(0).getJsonString("action"));
            for (int index = 1; index < jsonSendersArray.size(); index++)
            {
                System.out.println(jsonSendersArray.getJsonObject(index).getJsonNumber("fromUserId")
                        + " : "
                        + jsonSendersArray.getJsonObject(index).getJsonString("senderName"));
            }
        }
        return jsonSendersArray;
    }
    private JsonObject createFailedToAddMsg(Integer initiatedByAdminId, String userName)
    {
        JsonProvider provider = JsonProvider.provider();
        JsonObject failedToAddMsg = provider.createObjectBuilder()                
                .add("action", "showFailedToAddUserMsg")    
                .add("result", "Username :"+ userName+", already exist in the database, please try another name.")    
                .build();        
        return failedToAddMsg;
    }
    


    private void sendToSession(Session session, JsonObject message)
    {
        try
        {
            session.getBasicRemote().sendText(message.toString());
        } catch (IOException ex)
        {
            //comment by me
            //sessions.remove(session);
            Logger.getLogger(ChatSessionHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void sendJArrayToSession(Session session, JsonArray jsonArray)
    {
        try
        {
            session.getBasicRemote().sendText(jsonArray.toString());            
        } 
        catch (IOException ex)
        {
            //comment by me
            //sessions.remove(session);
            Logger.getLogger(ChatSessionHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
