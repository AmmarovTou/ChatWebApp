/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Forms;

/**
 *
 * @author AMMAROV
 */
public class UserDTO
{

    public UserDTO()
    {
    }

    public UserDTO(Integer userId, String userName)
    {
        this.userId = userId;
        this.userName = userName;
    }
    private Integer userId;
    private String userName;

    public Integer getUserId()
    {
        return userId;
    }

    public void setUserId(Integer userId)
    {
        this.userId = userId;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }
}
