package com.gempukku.stccg.chat;

import com.gempukku.stccg.async.handler.HTMLUtils;

public class HelpCommandCallback extends ChatCommandWithMessage {

    HelpCommandCallback(ChatRoomMediator mediator) {
        super(mediator);
    }

    @Override
    public void commandReceived(String from, String parameters, boolean admin) {
        String message = """
                    List of available commands:
                    /ignore username - Adds user 'username' to list of your ignores
                    /unignore username - Removes user 'username' from list of your ignores
                    /listIgnores - Lists all your ignored users
                    /incognito - Makes you incognito (not visible in user list)
                    /endIncognito - Turns your visibility 'on' again""";
        if (admin) {
            message += """
                        
                        
                        Admin only commands:
                        /ban username - Bans user 'username' permanently
                        /banIp ip - Bans specified ip permanently
                        /banIpRange ip - Bans ips with the specified prefix, ie. 10.10.10.""";
        }

        sendChatMessage(ChatStrings.SYSTEM_USER_ID, from, HTMLUtils.replaceNewlines(message));
    }
    
}