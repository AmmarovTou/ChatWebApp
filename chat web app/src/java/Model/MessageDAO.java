/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import Forms.UserDTO;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author AMMAROV
 */
public class MessageDAO
{
    public static ArrayList<Message> getMessagesBetween(Integer toUserId, Integer fromUserId, Connection connection)
    {
        ResultSet resultSet = null;
        Statement statement = null; 
        ArrayList<Message> listOfMessages = new ArrayList<>();
        Message messageObj = null;
        try
        {
            String sentBy = UserDAO.getUserNameWithId(connection, toUserId);
            
            statement = connection.createStatement();
            String SQL = "select FromId, ToId, Message from chatdb.messages WHERE ( ToId = '"+toUserId+"' AND FromId = '"+fromUserId+"' ) OR ( ToId = '"+fromUserId+"' AND FromId = '"+toUserId+"' ) ";            
            resultSet = statement.executeQuery(SQL);
            
            Integer fromId = 0;
            Integer toId = 0;
            String messageText = "";
            
            while (resultSet.next())
            {
                fromId = resultSet.getInt("FromId");
                toId = resultSet.getInt("ToId");
                
                if((fromId == fromUserId) && (toId == toUserId))
                {
                    messageText = "me: ";
                }
                else if((toId == fromUserId) && (fromId == toUserId))
                {
                    messageText = sentBy+": ";
                }
                    
                messageText += resultSet.getString("Message");

                messageObj = new Message(fromId, toId, messageText);                
                listOfMessages.add(messageObj);
                
                System.out.println("adding: message from id: "+fromId
                        +", message to id: "+toId
                        +", message text: "+messageText);
            }
        } 
        catch (SQLException ex)
        {
            Logger.getLogger(MessageDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listOfMessages;
    }
    
    public static ArrayList<UserDTO> getListOfSenders(Connection connection, Integer msgsUnreadByUserId)
    {
        ArrayList<UserDTO> listOfSenders = new ArrayList<>();
        ResultSet resultSet;
        Statement statement = null; 
        
        String lookupQuery = "select FromId from chatdb.messages WHERE ReadState = 0 AND ToId = '"+msgsUnreadByUserId+"' ";
        try
        {
            //connect to DB
            statement = connection.createStatement();
            resultSet = statement.executeQuery(lookupQuery);
            
            Integer senderId = 0;
            String senderName = "";
            UserDTO userDTO;                        
            
            while (resultSet.next())
            {
                senderId = resultSet.getInt("FromId");
                senderName = UserDAO.getUserNameWithId(connection, senderId);
                
                userDTO = new UserDTO(senderId, senderName);
                Integer foundAt = -1;
                for(int index = 0 ; index < listOfSenders.size() ; index++)
                {
                    if(senderId == listOfSenders.get(index).getUserId())
                    {
                        foundAt = index;
                    }
                }
                if(foundAt == -1)
                {
                    listOfSenders.add(userDTO);

                    System.out.println("adding to the list of senders, senderId: "+userDTO.getUserId()
                            +", senderName: "+userDTO.getUserName());
                }
            }            
        } 
        catch (SQLException ex)
        {
            System.out.println("getting list of senders failed: " + ex);
            Logger.getLogger(MessageDAO.class.getName()).log(Level.SEVERE, null, ex);
        }        
        return listOfSenders;
    }
    
    public static void markMsgsAsRead(Connection connection, Integer fromId, Integer toId)
    {        
        Statement statement = null;   
        String alterQuery = "update messages set ReadState = 1 WHERE FromId = '"+fromId+"' AND ToId = '"+toId+"' ";
        try
        {            
            statement = connection.createStatement();
            statement.executeUpdate(alterQuery);
        }
        catch (SQLException ex)
        {
            Logger.getLogger(MessageDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void insertMessage(Message msgObj, Connection connection)
    {        
        Statement statement;   
        try
        {
            statement = connection.createStatement();
            String SQL = "INSERT INTO chatdb.messages (`FromId`, `ToId`, `Message`, `Date`, `ReadState`) VALUES ('"+msgObj.getFromId()+"','"+msgObj.getToId()+"','"+msgObj.getMessageText()+"','"+msgObj.getMessageDate()+"','"+msgObj.getReadState()+"')";
            statement.executeUpdate(SQL);
            System.out.println("inserted into messages table: "+msgObj.toString()+", using connection ID:"+connection.toString());            
        }
        catch (SQLException ex)
        {
            Logger.getLogger(MessageDAO.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }       
    
}
