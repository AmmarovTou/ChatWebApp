/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WebSocket;

import javax.websocket.Session;

/**
 *
 * @author AMMAROV
 */
public class ConnectedUser
{
    public ConnectedUser()
    {
    }

    public ConnectedUser(Session session, Integer userId)
    {
        this.session = session;
        this.userId = userId;
    }
    
    private Session session;
    private Integer userId;

    public Session getSession()
    {
        return session;
    }

    public void setSession(Session session)
    {
        this.session = session;
    }

    public Integer getUserId()
    {
        return userId;
    }

    public void setUserId(Integer userId)
    {
        this.userId = userId;
    }
}
