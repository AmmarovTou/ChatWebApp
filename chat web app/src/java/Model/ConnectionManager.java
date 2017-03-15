/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author AMMAROV
 */
public class ConnectionManager
{
    static Connection connection;

    public static Connection getConnection()
    {
        try
        {
            String databaseUrl = "jdbc:mysql://localhost:3306/chatdb";
            String userName = "root";
            String password = "";
            
            
            //String url = "jdbc:odbc:" + "DataSource";
            String url = "jdbc:odbc:" + "chatdb";
            // assuming "DataSource" is your DataSource name

            //Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            Class.forName("com.mysql.jdbc.Driver");
            
            
            try
            {
                connection = DriverManager.getConnection(databaseUrl, userName, password);
            }
            catch (SQLException ex)
            {
                ex.printStackTrace();
            }
        }
        catch (Exception exc)
        {
            System.out.println(exc);
        }        
        return connection;
    }
}
