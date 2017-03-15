/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import Controllers.ServerController;
import Forms.UserDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
public class UserDAO
{
    
    static Connection currentConnection = null;
    static ResultSet resultSet = null;
    public static User logIn(Connection connection, User userObj)
    {
        Statement statement = null; 
        
        String username = userObj.getUserName();
        String password = userObj.getPassword();
        String lookupQuery = "select * from users where UserName='" + username + "' AND Password='" + password + "'";
        
        try
        { 
            //connect to DB
            statement = connection.createStatement();
            resultSet = statement.executeQuery(lookupQuery);
            boolean validResult = resultSet.next(); 
            // if user does not exist set the isValid variable to false
            if (!validResult)
            {
                System.out.println("Sorry, you are not a registered user! Please sign up first");
                userObj.setIsValid(false);
            } 
            //if user exists set the isValid variable to true
            else if (validResult)
            {
                Integer userId = resultSet.getInt("UserId");
                String userName = resultSet.getString("UserName");                
                System.out.println("Welcome " + userName);   
                userObj.setId(userId);
                userObj.setIsValid(true);
                
                Integer isAdminCode =  resultSet.getInt("IsAdmin");
                if(isAdminCode == 1)
                    userObj.setIsAdmin(1);
                else
                    userObj.setIsAdmin(0);
            }
        }
        catch(Exception exc)
        {
            System.out.println("Log In failed: An Exception has occurred! " + exc);
        }
        return userObj;
    }
    
    public static Integer insertUser(Connection connection, User userObj)
    {
        Integer resultCode = 0;
        Statement statement = null; 
        
        try
        {
            statement = connection.createStatement();
            
            PreparedStatement ps = connection.prepareStatement("select * from chatdb.users where UserName = ? ");
            ps.setString(1, userObj.getUserName());
            ResultSet rs = ps.executeQuery();
            if (rs.next())
            {
                resultCode = -1;
            } 
            else
            {
                String insertQuery = "INSERT INTO chatdb.users (`UserName`, `Password`, `IsAdmin`) VALUES ('"+userObj.getUserName()+"','"+userObj.getPassword()+"','"+userObj.getIsAdmin()+"')";
                resultCode = statement.executeUpdate(insertQuery);
                ServerController.shouldUpdateUsersCache = true;
            }
        } 
        catch (SQLException ex)
        {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return resultCode;
    }    
    
    public static ArrayList<User> returnAllUsers(Connection connection, String loggedInUserName)
    {       
        Statement statement = null;
        ArrayList<User> listOfUsers = new ArrayList<>();
        try
        {
            statement = connection.createStatement();
            resultSet = statement.executeQuery("select * from chatdb.users");
            
            User userObj;
            Integer userId = 0;
            String userName = "";
            while (resultSet.next())
            {
                userId = resultSet.getInt("UserId");
                userName = resultSet.getString("UserName");
                if(!userName.equals(loggedInUserName))
                {
                    userObj = new User(userId, userName);
                    listOfUsers.add(userObj);
                    System.out.println("adding: "+userId+", and: "+userName);
                }
            }
        } 
        catch (SQLException ex)
        {
            ex.printStackTrace();
            //Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listOfUsers;
    }
        
    public static Integer deleteUser(Connection connection, Integer selectedUserId)
    {
        Integer resultCode = -1;
        Statement statement;
        String deleteQuery = "DELETE FROM chatdb.users WHERE UserId = '"+selectedUserId+"' ";
        try
        {
            statement = connection.createStatement();
            resultCode = statement.executeUpdate(deleteQuery);  
            ServerController.shouldUpdateUsersCache = true;
        } 
        catch (SQLException ex)
        {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return resultCode;
    }
    
    public static String getUserNameWithId(Connection connection, Integer userId)
    {
        String userName = null;
        Statement statement = null; 
        String lookupQuery = "select UserName from users where UserId='" + userId + "'";
        
        try
        { 
            //connect to DB
            statement = connection.createStatement();
            resultSet = statement.executeQuery(lookupQuery);
            
            boolean validResult = resultSet.next(); 
            if(validResult)
            {
                userName = resultSet.getString("UserName");  
            }
        }
        catch(Exception exc)
        {
            System.out.println("getting user name failed, an Exception has occurred!: " + exc);
        }
        return userName;
    }
    
    public static Integer getUserIdWithName(Connection connection, String userName)
    {
        Integer userId = -1;
        Statement statement = null; 
        String lookupQuery = "select UserId from users where UserName='" + userName + "'";
        
        try
        { 
            //connect to DB
            statement = connection.createStatement();
            resultSet = statement.executeQuery(lookupQuery);
            
            boolean validResult = resultSet.next(); 
            if(validResult)
            {
                userId = resultSet.getInt("UserId");  
            }
        }
        catch(Exception exc)
        {
            System.out.println("getting user name failed, an Exception has occurred!: " + exc);
        }
        return userId;
    }
    
    public static Boolean checkIfAdmin(Connection connection, Integer initiatedById)
    {
        Boolean isAdminFlag = false;
        Statement statement = null;                 
        
        String lookupQuery = "select * from users where UserId='" + initiatedById +"'";
        try
        { 
            //connect to DB
            statement = connection.createStatement();
            resultSet = statement.executeQuery(lookupQuery);
            boolean validResult = resultSet.next(); 
            if (!validResult)
            {
                isAdminFlag = false;
            } 
            //if user exists set the isValid variable to true
            else if (validResult)
            {
                isAdminFlag = true;
            }
        }
        catch(Exception exc)
        {
            System.out.println("checking if action has been initiated by an admin has failed!: " + exc);
        }
        return isAdminFlag;
    }
}
