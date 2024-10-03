import GempClientCommunication from './communication.js';
import { monthNames } from './common.js';

export default class ChatBoxUI {
    name;
    userInfo;
    userName;
    pingRegex;
    mentionRegex;
    everyoneRegex = new RegExp('/(@everyone|@anyone)/');
    div;
    comm;

    chatMessagesDiv;
    chatTalkDiv;
    chatListDiv;

    showTimestamps = false;
    maxMessageCount = 500;
    talkBoxHeight = 25;

    chatUpdateInterval = 100;

    playerListener;
    hiddenClasses;

    hideSystemButton;

    lockChat = false;
    stopUpdates = false;
    
    dialogListener;
    
    enableDiscord = false;
    discordDiv;
    discordWidget;
    chatEmbed;
    displayDiscord = true;
    
    toggleChatButton;
    
    constructor(name, div, url, showList, playerListener, showHideSystemButton, displayChatListener, allowDiscord=false) {
        var that = this;
        this.hiddenClasses = new Array();
        this.playerListener = playerListener;
        this.dialogListener = displayChatListener;
        this.name = name;
        this.div = div;
        
        //This needs to be done before the comm object is instantiated, as otherwise it's too slow for immediate errors

        if(this.name == "Game Hall")
            {
                this.chatMessagesDiv = $("#chatMessages");
            }
            else
            {
                this.chatMessagesDiv = $("<div class='chatMessages'></div>");
                this.div.append(this.chatMessagesDiv);
            }
    
            
            this.comm = new GempClientCommunication(url, function (xhr, ajaxOptions, thrownError) {
                that.appendMessage("Unknown chat problem occurred (error=" + xhr.status + ")", "warningMessage");
            });
            this.enableDiscord = allowDiscord;
    
            this.comm.getPlayerInfo(function(json)
            { 
                that.initPlayerInfo(json);
            }, this.chatErrorMap());
    
            if (this.name != null) {
                
                if(this.name == "Game Hall")
                {
                    this.discordDiv = $("#discordChat");
    
                    this.chatTalkDiv = $("#chatTalk");
    
                    this.hideSystemButton = $("#showSystemButton");
                    if (showHideSystemButton) {
                        hideSystemButton.button({icons:{
                             primary:"ui-icon-zoomin"
                         }, text:false});
    
                        this.hideSystemButton.click(
                                function () {
                                    if (that.isShowingMessageClass("systemMessage")) {
                                        $('#showSystemMessages').button("option", "icons", {primary:'ui-icon-zoomin'});
                                        that.hideMessageClass("systemMessage");
                                    } else {
                                        $('#showSystemMessages').button("option", "icons", {primary:'ui-icon-zoomout'});
                                        that.showMessageClass("systemMessage");
                                    }
                                });
                        this.hideMessageClass("systemMessage");
                    }
                    else
                    {
                        this.hideSystemButton.hide();
                        this.hideSystemButton = null;
                    }
    
                    this.comm.startChat(this.name,
                            function (xml) {
                                that.processMessages(xml, true);
                                that.scrollChatToBottom();
                            }, this.chatErrorMap());
    
                    this.chatTalkDiv.keydown(function (e) {
                        if (e.keyCode == 13) {
                            if(!e.shiftKey)
                            {
                                e.preventDefault();
                                var value = $(this).val();
                                if (value != "")
                                    that.sendMessage(value);
                                $(this).val("").trigger("oninput");
                                that.scrollChatToBottom();
                            }
                        }
                    });
    
                    
                    if (showList) {
                        this.chatListDiv = $("#userList");
                        this.toggleChatButton = $("#toggleChatButt");
    
                        this.toggleChatButton.button();
                        this.toggleChatButton.click( function() {
                            that.toggleChat();
                        });
                    }
                    
                    this.setDiscordVisible(false);
                }
                else
                {
                    this.chatTalkDiv = $("<input type='text' class='chatTalk'>");
    
                    if (showHideSystemButton) {
                        this.hideSystemButton = $("<button id='showSystemMessages'>Toggle system messages</button>").button(
                        {icons:{
                            primary:"ui-icon-zoomin"
                        }, text:false});
                        this.hideSystemButton.click(
                                function () {
                                    if (that.isShowingMessageClass("systemMessage")) {
                                        $('#showSystemMessages').button("option", "icons", {primary:'ui-icon-zoomin'});
                                        that.hideMessageClass("systemMessage");
                                    } else {
                                        $('#showSystemMessages').button("option", "icons", {primary:'ui-icon-zoomout'});
                                        that.showMessageClass("systemMessage");
                                    }
                                });
                        this.hideMessageClass("systemMessage");
                    }
    
                    if (showList) {
                        this.chatListDiv = $("<div class='userList'></div>");
                        this.div.append(this.chatListDiv);
                    }
                    if (this.hideSystemButton != null)
                        this.div.append(this.hideSystemButton);
    
                    this.div.append(this.chatTalkDiv);
    
                    this.comm.startChat(this.name,
                            function (xml) {
                                that.processMessages(xml, true);
                            }, this.chatErrorMap());
    
                    this.chatTalkDiv.bind("keypress", function (e) {
                        var code = (e.keyCode ? e.keyCode : e.which);
                        if (code == 13) {
                            var value = $(this).val();
                            if (value != "")
                                that.sendMessage(value);
                            $(this).val("");
                        }
                    });
                }
                
            } else {
                this.talkBoxHeight = 0;
            }
    }
    
    initPlayerInfo(playerInfo) {
        this.userInfo = playerInfo;
        this.userName = this.userInfo.name; 
        this.pingRegex = new RegExp("@" + this.userName + "\\b");
        this.mentionRegex = new RegExp("(?<!<b>)\\b" + this.userName + "\\b");
    }


    hideMessageClass(msgClass) {
        this.hiddenClasses.push(msgClass);
        $("div.message." + msgClass, this.chatMessagesDiv).hide();
    }

    isShowingMessageClass(msgClass) {
        var index = $.inArray(msgClass, this.hiddenClasses);
        return index == -1;
    }

    showMessageClass(msgClass) {
        var index = $.inArray(msgClass, this.hiddenClasses);
        if (index > -1) {
            this.hiddenClasses.splice(index, 1);
            $("div.message." + msgClass, this.chatMessagesDiv).show();
        }
    }

    setBounds(x, y, width, height) {
        
        if(this.name != "Game Hall")
        {
            var talkBoxPadding = 3;

            var userListWidth = 150;
            if (this.chatListDiv == null)
               userListWidth = 0;

            if (this.chatListDiv != null)
               this.chatListDiv.css({ position:"absolute", left:x + width - userListWidth + "px", top:y + "px", width:userListWidth, height:height - this.talkBoxHeight - 3 * talkBoxPadding, overflow:"auto" });
           
            if(this.chatMessagesDiv != null)
                this.chatMessagesDiv.css({ position:"absolute", left:x + "px", top:y + "px", width:width - userListWidth, height:height - this.talkBoxHeight - 3 * talkBoxPadding, overflow:"auto" });
            
            if (this.chatTalkDiv != null) {
               var leftTextBoxPadding = 0;

               if (this.hideSystemButton != null) {
                   this.hideSystemButton.css({position:"absolute", left:x + width - talkBoxPadding - this.talkBoxHeight + "px", top:y - 2 * talkBoxPadding + (height - this.talkBoxHeight) + "px", width:this.talkBoxHeight, height:this.talkBoxHeight});
                   leftTextBoxPadding += this.talkBoxHeight + talkBoxPadding;
               }
               // if (this.lockButton != null) {
               //     this.lockButton.css({position:"absolute", left:x + width - talkBoxPadding - this.talkBoxHeight - leftTextBoxPadding + "px", top:y - 2 * talkBoxPadding + (height - this.talkBoxHeight) + "px", width:this.talkBoxHeight, height:this.talkBoxHeight});
               //     leftTextBoxPadding += this.talkBoxHeight + talkBoxPadding;
               // }

               this.chatTalkDiv.css({ position:"absolute", left:x + talkBoxPadding + "px", top:y - 2 * talkBoxPadding + (height - this.talkBoxHeight) + "px", width:width - 3 * talkBoxPadding - leftTextBoxPadding, height:this.talkBoxHeight });
            }
        }

        this.handleChatVisibility();       
    }
    
    handleChatVisibility() {
        
        if(this.enableDiscord)
        {
            if(this.displayDiscord)
            {
                this.toggleChatButton.text("Switch to Legacy");
                
                if(this.chatEmbed == null)
                {
                    this.discordDiv.show();
                    this.chatEmbed = $("<widgetbot server='699957633121255515' channel='873065954609881140' width='100%' height='100%' username='" + this.userName + "'></widgetbot>");
                    var script = $("<script src='https://cdn.jsdelivr.net/npm/@widgetbot/html-embed'></script>");
                    this.discordDiv.append(script);
                    this.discordDiv.append(this.chatEmbed);
                }
            }
            else
            {
                this.toggleChatButton.text("Switch to Discord");
            } 
        }
        
        if(this.enableDiscord && this.displayDiscord)
        {
            if(this.discordDiv != null)
                this.discordDiv.show();
            
            if(this.chatMessagesDiv != null)
                this.chatMessagesDiv.hide();
            if(this.chatTalkDiv != null)
                this.chatTalkDiv.hide();
            if(this.hideSystemButton != null)
                this.hideSystemButton.hide();
            // if(this.lockButton != null)
            //     this.lockButton.hide();
        }
        else
        {
            if(this.discordDiv != null)
                this.discordDiv.hide();
            
            if(this.chatMessagesDiv != null)
                this.chatMessagesDiv.show();
            if(this.chatTalkDiv != null)
                this.chatTalkDiv.show();
            if(this.hideSystemButton != null)
                this.hideSystemButton.show();
            // if(this.lockButton != null)
            //     this.lockButton.show(); 
        }
        
    }
    
    toggleChat(){
        this.setDiscordVisible(!this.displayDiscord);
    }
    
    setDiscordVisible(visible) {
      this.displayDiscord = visible;
      
      this.handleChatVisibility();
        
    }
    
    checkForEnd(message, msgClass) {
        // if(msgClass != "systemMessage")
        // {
        //     return;
        // }
        
        if(message.includes("Thank you for playtesting!")) {
            if (this.dialogListener != null) {
                this.dialogListener("Give us feedback!", message);
            }
        }
    }
    

    appendMessage(message, msgClass) {
        if (msgClass == undefined)
            msgClass = "chatMessage";
        
        var locked = false;
        var scroll = this.chatMessagesDiv.scrollTop();
        var maxScroll = this.chatMessagesDiv[0].scrollHeight - this.chatMessagesDiv.outerHeight();
        var noScrollBars = maxScroll <= 0;
        var ratio = scroll / maxScroll;
        
        if(msgClass === "warningMessage" || noScrollBars || maxScroll <= 30 || ratio >= 0.999)
            locked = true;
        
        if(this.pingRegex != null && this.pingRegex.test(message))
        {
            msgClass += " user-ping";
        }
        else if((this.mentionRegex != null && this.mentionRegex.test(message)) || this.everyoneRegex.test(message))
        {
            msgClass += " user-mention";
        }
        
        if(msgClass == "gameMessage")
        {
            message = "<div class='msg-content'>" + message + "</div>";
        }
        
        var messageDiv = $("<div class='message " + msgClass + "'>" + message + "</div>");

        this.chatMessagesDiv.append(messageDiv);
        var index = $.inArray(msgClass, this.hiddenClasses);
        if (!this.isShowingMessageClass(msgClass)) {
            messageDiv.hide();
        }

        if ($("div.message", this.chatMessagesDiv).length > this.maxMessageCount) {
            $("div.message", this.chatMessagesDiv).first().remove();
        }
        
        if(locked)
            this.scrollChatToBottom();
        
        this.checkForEnd(message, msgClass);
    }

    appendNotLoggedIntoGameMessage() {
        message = "Game problem - You're not logged in. Go to the <a href='index.html'>main page</a> to log in.";
        msgClass = "warningMessage";
        this.appendMessage(message, msgClass);
    }

    appendServerCommunicationProblemMessage(xhr_status) {
        message = "There was a problem communicating with the server" + xhr_status + ". " +
            "If the game is finished, it has been removed. Otherwise, you have lost connection to the server.";
        this.appendMessage(message, "warningMessage");
        message = "Refresh the page (press F5) to resume the game, " +
            "or press back on your browser to get back to the Game Hall.";
        this.appendMessage(message, "warningMessage");
    }

    scrollChatToBottom() {
        this.chatMessagesDiv.prop({ scrollTop:this.chatMessagesDiv.prop("scrollHeight") })
    }
    
    formatToTwoDigits(no) {
        if (no < 10)
            return "0" + no;
        else
            return no;
    }

    processMessages(xml, processAgain) {
        var root = xml.documentElement;
        if (root.tagName == 'chat') {
            this.retryCount = 0;
            var messages = root.getElementsByTagName("message");
            for (var i = 0; i < messages.length; i++) {
                var message = messages[i];
                var from = message.getAttribute("from");
                var text = message.childNodes[0].nodeValue;

                var msgClass = "chatMessage";
                if (from == "System")
                    msgClass = "systemMessage";
                var prefix = "<div class='msg-identifier'>";
                if (this.showTimestamps) {
                    var date = new Date(parseInt(message.getAttribute("date")));
                    var dateStr = monthNames[date.getMonth()] + " " + date.getDate() + " " + this.formatToTwoDigits(date.getHours()) + ":" + this.formatToTwoDigits(date.getMinutes()) + ":" + this.formatToTwoDigits(date.getSeconds());
                    prefix += "<span class='timestamp'>[" + dateStr + "]</span>";
                }
                
                prefix += "<span> <b>" + from + ": </b></span></div>";
                var postfix = "<div class='msg-content'>" + text + "</div>";
                    
                this.appendMessage(prefix + postfix, msgClass);
            }

            var users = root.getElementsByTagName("user");
            if (this.playerListener != null) {
                var players = new Array();
                for (var i = 0; i < users.length; i++) {
                    var user = users[i];
                    var userName = user.childNodes[0].nodeValue;
                    players.push(userName);
                }
                this.playerListener(players);
            }

            if (this.chatListDiv != null) {
                this.chatListDiv.html("");
                for (var i = 0; i < users.length; i++) {
                    var user = users[i];
                    var userName = user.childNodes[0].nodeValue;
                    this.chatListDiv.append("<div class='chatUser'>" + userName + "</div>");
                }
            }

            var that = this;

            if (processAgain)
                setTimeout(function () {
                    that.updateChatMessages();
                }, that.chatUpdateInterval);
        }
    }

    updateChatMessages() {
        var that = this;

        this.comm.updateChat(this.name, function (xml) {
            that.processMessages(xml, true);
        }, this.chatErrorMap());
    }

    sendMessage(message) {
        var that = this;
        this.comm.sendChatMessage(this.name, message, this.chatErrorMap());
        
        //this.chatEmbed.emit("sendMessage", message);
    }

    chatMalfunction() {
        this.stopUpdates = true;
        this.chatTalkDiv.prop('disabled', true);
        this.chatTalkDiv.css({"background-color": "#ff9999"});
        
        if(this.discordDiv)
        {
            this.discordDiv.prop('disabled', true);
            this.discordDiv.css({"background-color": "#ff9999"});
        }
    }

    chatErrorMap() {
        var that = this;
        return {
            "0":function() {
                that.chatMalfunction();
                that.appendMessage("Chat server has been closed or there was a problem with your internet connection.", "warningMessage");
            },
            "401":function() {
                that.chatMalfunction();
                that.appendMessage("You are not logged in.", "warningMessage");
            },
            "403": function() {
                that.chatMalfunction();
                that.appendMessage("You have no permission to participate in this chat.", "warningMessage");
            },
            "404": function() {
                that.chatMalfunction();
                that.appendMessage("Chat room is closed.", "warningMessage");
            },
            "410": function() {
                that.chatMalfunction();
                that.appendMessage("You have been inactive for too long and were removed from the chat room. Refresh the page if you wish to re-enter.", "warningMessage");
            }
        };
    }
}
