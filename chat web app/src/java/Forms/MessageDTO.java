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
public class MessageDTO
{
    public MessageDTO()
    {
    }

    public MessageDTO(String sentBy, Integer fromId, Integer toId, String messageText, String messageDate, Integer readState)
    {
        this.sentBy = sentBy;
        this.fromId = fromId;
        this.toId = toId;
        this.messageText = messageText;
        this.messageDate = messageDate;
        this.readState = readState;
    }
    private String sentBy;
    private Integer fromId;
    private Integer toId;
    private String messageText;        
    private String messageDate;
    private Integer readState;

    public String getSentBy()
    {
        return sentBy;
    }

    public void setSentBy(String sentBy)
    {
        this.sentBy = sentBy;
    }

    public Integer getFromId()
    {
        return fromId;
    }

    public void setFromId(Integer fromId)
    {
        this.fromId = fromId;
    }

    public Integer getToId()
    {
        return toId;
    }

    public void setToId(Integer toId)
    {
        this.toId = toId;
    }

    public String getMessageText()
    {
        return messageText;
    }

    public void setMessageText(String messageText)
    {
        this.messageText = messageText;
    }

    public String getMessageDate()
    {
        return messageDate;
    }

    public void setMessageDate(String messageDate)
    {
        this.messageDate = messageDate;
    }

    public Integer getReadState()
    {
        return readState;
    }

    public void setReadState(Integer readState)
    {
        this.readState = readState;
    }
    
    @Override
    public String toString()
    {
        return "MessageDTO{" + "sentBy=" + sentBy + ", fromId=" + fromId + ", toId=" + toId + ", messageText=" + messageText + ", messageDate=" + messageDate + ", readState=" + readState + '}';
    }
}
