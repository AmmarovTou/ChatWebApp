var socket = new WebSocket("ws://localhost:8080/chat web app/chat");
socket.onmessage = onMessage;
var sendersIdsList = [];

//called whenever there's a message incoming from web socket by the other end(the server)
//normally it's a JSON message, checks the "action" and process the message accordingly
function onMessage(event) {
    
    var messageFromServer = JSON.parse(event.data);
    
    if(messageFromServer.action === "someOneIsTyping") {
        processSomeOneIsTyping(messageFromServer);
    }
        
    if (messageFromServer.action === "receiveMessage") {        
        processReceivedMsg(messageFromServer);
    }
    
    if(messageFromServer.action === "recieveNewUser") {
        processNewAddedUser(messageFromServer);
    }
    
    if(messageFromServer.action === "showFailedToAddUserMsg") {
        processFailedToAddUser(messageFromServer);
    }
    
    if(messageFromServer.action === "userDeleted") {
        processRemovedUser(messageFromServer);
    }
    
    if(messageFromServer.action === "appendSentMessage") {
        //var messageText = messageFromServer.messageText;
        appendMessage(messageFromServer);
    }
    
    if (messageFromServer[0].action === "processSenders") {     
        var receivedArrOfSenders = []
        receivedArrOfSenders = messageFromServer;
        processReceivedSenders(receivedArrOfSenders);
    }
    
}

//send a new message to the destination user through the server
function sendMessage(fromId, toId, messageText)
{
    var message = {
        action: "sendMessage",
        fromId: fromId,
        toId: toId,
        messageText: messageText
    };
    //alert("will try to send JSON with action: sendMessage, fromId"+fromId+", toId: "+toId+", messageText: "+messageText);
    socket.send(JSON.stringify(message));
}

//to show some one is typing effect 
function sendSomeOneIsTyping()
{
    var form = document.getElementById("chattingForm");
    var senderId = parseInt(form.elements["fromId"].value);  
    
    var intSize = parseInt($(selectedListOfUsers).size());
    if(intSize > 0)
    {
        var destinationId = parseInt($(selectedListOfUsers).last()[0]);     
        var userName = form.elements["loggedInUserName"].value;
        var message = {
            action: "someOneIsTyping",
            fromId: senderId,
            senderName: userName,
            toId: destinationId
        };
        //alert("will try to send JSON with action: someOneIsTyping, senderName: "+userName+", fromId"+senderId+", toId: "+destinationId);
        socket.send(JSON.stringify(message));
    }
}

//shows some on is typing, a realtime effect to get a sense if the other client is writing a message to us
function processSomeOneIsTyping(messageFromServer)
{
    var userId = parseInt(messageFromServer.userId);
    var userName = messageFromServer.userName;
    var intSize = parseInt($(selectedListOfUsers).size());
    if(intSize > 0)
    {
        var destinationId = parseInt($(selectedListOfUsers).last()[0]);  
        if(destinationId === userId)
        {
            $("#textChangedDiv").text(userName+" is typing");
            alert(userName+" is typing");
            //timer wait three seconds then clear text
        }
    }
}

//adds a new user through the server, and all connected clients get the update
function AddNewUserByAdminSocket()
{
    var form = document.getElementById("addNewUserForm");
    var userName = form.elements["userName"].value;
    var password = form.elements["password"].value;        
    
    var isAdminCode = 0;
    if ($('#isAdmin').is(":checked"))
    {
        isAdminCode = 1;
    }
    //alert("userName: "+userName+", password: "+password+", isAdminChk: "+isAdminCode);
    
    var adminForm = document.getElementById("adminForm");
    var fromId = adminForm.elements["fromId"].value;
    var initiatedById = parseInt(fromId);            
    
    var message = {
        action: "addNewUser",
        initiatedById: initiatedById,
        userName: userName,
        password: password,
        isAdminCode: isAdminCode
    };    
    /*
    alert("will try to send JSON with action: addNewUser, fromId"+fromId
            +", userName: "+userName+", password: "+password
            +", isAdminCode: "+isAdminCode);  
    */
    socket.send(JSON.stringify(message));
    
}

//process the notification to add a new user to the list of users view
function processNewAddedUser(messageFromServer)
{
    //alert("entered processNewAddedUser scope");
    
    var userId = parseInt(messageFromServer.userId);
    var userName = messageFromServer.userName;
    appendNewUser(userId, userName);
    /*
    var initiator = parseInt(messageFromServer.initiator);
    
    //at a user home page, it's tryying to retrieve a form with id "adminForm", which only exists in the admin home page
    //then it's parsing a NaN value, because the whole form doesn't exist, then whenever a new user gets added by an admin
    //the client at user home page will not appened the new user because it's hanged with an error.
    //thus there's no need for the admin feedback
    var form = document.getElementById("adminForm");
    var fromId = form.elements["fromId"].value;
    var intFromId = parseInt(fromId);   
    
    var result = messageFromServer.result;   
    
    var userId = parseInt(messageFromServer.userId);
    var userName = messageFromServer.userName;
    
    if(initiator === intFromId)
    {
        
        //alert("admin window: should add a new user as an option with, userId: "+userId+", userName: "+userName);
        alert("admin window: "+result);
        appendNewUser(userId, userName);
    }
    else
    {
        //alert("client window: should add a new user as an option with, userId: "+userId+", userName: "+userName);
        alert("client window: "+result);
        appendNewUser(userId, userName);
    }
    */
}

//appends the recieved newly added user's data to the view
function appendNewUser(userId, userName)
{
    $('#usersList').append($('<option>', {
            value: userId,
            text: userName
        }));
}

//if adding a new user has failed(this message only appears to the user who sent the request, more accurately the admin)
function processFailedToAddUser(messageFromServer)
{
    var errorMsg = messageFromServer.result;
    alert(errorMsg);
}

//removes a user through the server, and all connected clients get the update
function removeUserThroughSocket(selectedListOfUsers)
{
    if (selectedListOfUsers.length > 0)
    {
        var lastSelectedUser = $(selectedListOfUsers).last()[0];        
        
        var adminForm = document.getElementById("adminForm");
        var fromId = adminForm.elements["fromId"].value;
        var initiatedById = parseInt(fromId);            
    
        var userId = parseInt(lastSelectedUser);
        var message = {
            action: "removeUser",
            initiatedById: initiatedById,
            userId: userId
        };
        //alert("will send selected user id to be deleted: "+userId);
        socket.send(JSON.stringify(message));
    }
}

//process the notification to remove a user from the list of users view
function processRemovedUser(messageFromServer)
{
    var deletedUserId = parseInt(messageFromServer.deletedUserId);
    var resultMessage = messageFromServer.result;
    
    var selectedUsers = "";
    var selectedListOfUsers = new Array();
    $( "select option:selected" ).each(function() 
            {
                selectedListOfUsers.push($( this ).val());
                $("#selectedListOfUsers").val(selectedListOfUsers);
                selectedUsers += $( this ).val() +":"+ $( this ).text() + ",";
            });
    
    var index;
    for(index = 0 ; index < $(selectedListOfUsers).size() ; index++)
    {
        var intUserId = parseInt(selectedListOfUsers[index]);
        var delIndex = -1;
        if(intUserId === deletedUserId)
        {
            /*
            alert("selectedListOfUser size b4 deletion: "+$(selectedListOfUsers).size()
                    +"deletedUserId found in the list of selected users at inedx: "
                    +index+", and it should be deleted from the list");
            */
            delIndex = index;
        }
        //if the deleted is being selected by other clients, then clear selection and lists, then remove the option
        if(delIndex !== -1)
        {
            if($(selectedListOfUsers).size() > 0)
            {
                selectedListOfUsers.pop(selectedListOfUsers[index]);
                //alert("selectedListOfUser size after deletion: "+$(selectedListOfUsers).size());
            }
            
            $( "#usersList option" ).each(function()
            {
                var intIdChk = parseInt($(this).val());
                if (intIdChk === deletedUserId)
                {
                    alert("user: "+$(this).text()+" has been removed");
                    $(this).remove();
                    $("#selectionDiv").text(" ");
                    $("#lastSelectedDiv").text(" ");
                    selectedListOfUsers = new Array();
                    $("#selectedListOfUsers").val("");
                }
            });
        }
        //else the deleted user is not being selected by other clients, then just remove the option wihtout clearing lists
        else
        {
            $( "#usersList option" ).each(function()
            {
                var intIdChk = parseInt($(this).val());
                if (intIdChk === deletedUserId)
                {
                    //alert("user: "+$(this).text()+" has been removed");
                    $(this).remove();
                }
            });
        }
    }//end of for
    
    //if no user was selected
    $( "#usersList option" ).each(function()
            {
                var intIdChk = parseInt($(this).val());
                if (intIdChk === deletedUserId)
                {
                    //alert("user: "+$(this).text()+" has been removed");
                    $(this).remove();
                }
            });
}

//processes and appends the recieved message to the messages view
function processReceivedMsg(receivedMessage)
{
    var fromId = parseInt(receivedMessage.fromId);
    if (selectedListOfUsers.length > 0)
    {
        var lastSelectedUser = $(selectedListOfUsers).last()[0];        
        var lastSelectedInt = parseInt(lastSelectedUser);
        if(lastSelectedInt === fromId)
        {
            //$("#messagesBox").append(receivedMessage.messageText);
            var messagesBox = $('#messagesBox');
            var sentBy = receivedMessage.sentBy;
            var separator = ": ";
            var formattedPrefix = sentBy.concat(separator);
            var messageTxt = receivedMessage.messageText;
            var textToAppend = formattedPrefix.concat(messageTxt);
            messagesBox.val(messagesBox.val() + textToAppend);      
            markMsgsAsRead();
            //alert("inner-if: should append message text, fromId: "+fromId+", lastSelectedUser: "+lastSelectedInt+", messageText: "+textToAppend);
        }
        else
        {
            //make user bold
            $('#usersList option').each(function ()
            {
                var intUserId = parseInt(this.value);
                if (intUserId === fromId)
                {
                    $(this).css("font-weight", "bold");
                    //alert( "trying to make option at index: "+ currentOptIndex+" as bold");
                }
            });
            //alert("inner-else: fromId: "+fromId+", lastSelectedUser: "+lastSelectedInt+", messageText from server: "+receivedMessage.messageText);
        }
    }
    else
    {
        //make user bold
        $('#usersList option').each(function ()
        {
            var intUserId = parseInt(this.value);
            if (intUserId === fromId)
            {
                $(this).css("font-weight", "bold");
                //alert( "trying to make option at index: "+ currentOptIndex+" as bold");
            }
        });
        //alert("outer-else: received messageText from server: "+receivedMessage.messageText);
    }
}

//make the users elements which the current user has incoming unread messages from them as bold
function makeSendersBold(sendersIdsList)
{
    var usersOptions = []    
    $('#usersList option').each(function()
    {
        //var form = document.getElementById("chattingForm");
        //var unreadMsgsToUserId = form.elements["fromId"].value;
        var intUserId = parseInt(this.value);
        usersOptions.push( intUserId );
        //alert( "adding: "+ intUserId);
        
    });
    
    var index;    
    for (index = 0; index < sendersIdsList.length; index++)
    {
        var currentOptIndex = 0;
        for (currentOptIndex = 0; currentOptIndex < usersOptions.length; currentOptIndex++)
        {
            if(usersOptions[currentOptIndex] === sendersIdsList[index])
            {
                //alert( "option at index: "+currentOptIndex+", with userId: "+usersOptions[currentOptIndex]+" should become bold");        
                $('#usersList option').each(function ()
                {
                    var intUserId = parseInt(this.value);
                    if(intUserId === usersOptions[currentOptIndex])
                    {
                        $(this).css("font-weight","bold");
                        //alert( "trying to make option at index: "+ currentOptIndex+" as bold");
                    }
                });
            }
        }        
    }
}

//called on send new message button click
function formSubmit() 
{
    var form = document.getElementById("chattingForm");
    var fromId = form.elements["fromId"].value;
    var messageText = form.elements["messageText"].value;     
      
    if(messageText.length > 0)
    {
        //alert("selectedListOfUsers length: "+selectedListOfUsers.length);
        
        //appendMessage(messageText);
        
        var index;
        for(index = 0 ; index < selectedListOfUsers.length ; index++)
        {
            var toId = selectedListOfUsers[index];
            sendMessage(fromId, toId, messageText);
            //alert("trying to call sendMessage with fromId: "+fromId+", toId: "+toId+", messageText: "+messageText);
        }
    }
    clearMessageTextBox();   
}

//appends the successfully sent message to the messages view
function appendMessage(messageFromServer) 
{
    var toId = parseInt(messageFromServer.toId);
    if (selectedListOfUsers.length > 0)
    {
        var lastSelectedUser = $(selectedListOfUsers).last()[0];        
        var lastSelectedInt = parseInt(lastSelectedUser);
        if(lastSelectedInt === toId)
        {
            var messagesBox = $('#messagesBox');
            var me = messageFromServer.sentBy;
            var messageTxt = messageFromServer.messageText;
            var textToAppend = me.concat(messageTxt);
            messagesBox.val(messagesBox.val() + textToAppend);
            //alert(textToAppend+" should be added to the messages box");
        }
    }
}

//register as new user
function registerUser()
{
    var form = document.getElementById("logRegForm");
    var userName = form.elements["userReg"].value;
    var password = form.elements["passReg"].value;
    var returnedUserName = "";
    var regReq = $.post('AccountController', {registerUser: "registerUser",userName:"userName",password:"password"}, function (data)
    {
        returnedUserName = data;        
    })
            .done(function () {
                alert("alert user has been registered successfully with userName: "+returnedUserName);
            });
}

//whenever a new message has been sent
function clearMessageTextBox()
{
    $("#messageText").val("");
}

//send a request to the server to get the sent users who sent us messages, and these messages are still unread by us
function getSendersThroughSocket()
{
    var form = document.getElementById("chattingForm");
    var unreadMsgsToUserId = form.elements["fromId"].value;
                    
    var message = {
        action: "getSenders",
        unreadMsgsToUserId: unreadMsgsToUserId 
    };
    //alert("will try to send JSON with action: getSenders, unreadMsgsToUserId: " + unreadMsgsToUserId );
    socket.send(JSON.stringify(message));
}

//processes the recieved users who sent us some messages, and these messages are still unread by us
function processReceivedSenders(receivedArrOfSenders)
{
    var index;
    sendersIdsList = [];
    for (index = 1; index < receivedArrOfSenders.length; index++)
    {
        var element = receivedArrOfSenders[index];        
        var senderId = parseInt(element.fromUserId);
        var senderName = element.senderName;
        //alert("element at index: "+index+", contents senderId: "+senderId+", senderName: "+senderName);        
        sendersIdsList.push(senderId);
    }
    makeSendersBold(sendersIdsList);
}

//mark the messages between us and the selected user as read
function markMsgsAsRead()
{
    var form = document.getElementById("chattingForm");
    var unreadMsgsToUserId = form.elements["fromId"].value;
    var lastSelectedUser;
    
    if (selectedListOfUsers.length > 0)
    {
        lastSelectedUser = $(selectedListOfUsers).last()[0];  
        
        var msgsToUserId = parseInt(unreadMsgsToUserId);
        var msgsFromUserId = parseInt(lastSelectedUser);

        var message = {
            action: "markMsgsAsRead",
            msgsFromUserId: msgsFromUserId,
            msgsToUserId: msgsToUserId
        };
        //alert("will try to send JSON with action: markMsgsAsRead, msgsFromUserId: " + msgsFromUserId + ", msgsToUserId: " + msgsToUserId);
        socket.send(JSON.stringify(message));
    }    
}