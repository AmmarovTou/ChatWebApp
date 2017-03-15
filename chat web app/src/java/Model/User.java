/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

/**
 *
 * @author AMMAROV
 */
public class User
{   
    public User()
    {
    }
    
    public User(Integer Id, String userName)
    {
        this.Id = Id;
        this.userName = userName;
    }
    
    public User(String userName, String password, Integer isAdmin)
    {
        this.userName = userName;
        this.password = password;
        this.isAdmin = isAdmin;
    }
    
    private Integer Id;
    private String userName;
    private String password;
    private Boolean isValid;
    private Integer isAdmin;

    public Integer getId()
    {
        return Id;
    }

    public void setId(Integer Id)
    {
        this.Id = Id;
    }
    
    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public Boolean getIsValid()
    {
        return isValid;
    }

    public void setIsValid(Boolean isValid)
    {
        this.isValid = isValid;
    }

    public Integer getIsAdmin()
    {
        return isAdmin;
    }

    public void setIsAdmin(Integer isAdmin)
    {
        this.isAdmin = isAdmin;
    }
    
    @Override
    public String toString()
    {
        if(this.getId() != null)
            return "User{" + "Id=" + Id + ", userName=" + userName + ", password=" + password + ", isAdmin=" + isAdmin + '}';
        else
            return "User{" + "userName=" + userName + ", password=" + password + ", isAdmin=" + isAdmin + '}';
    }
}
