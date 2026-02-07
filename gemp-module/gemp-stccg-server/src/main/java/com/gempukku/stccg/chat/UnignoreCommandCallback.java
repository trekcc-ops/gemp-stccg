package com.gempukku.stccg.chat;

import com.gempukku.stccg.service.AdminService;

public class UnignoreCommandCallback extends ChatCommandWithMessage {
    
    private final AdminService _adminService;
    private static final int MAX_PLAYER_ID_LENGTH = 30;


    UnignoreCommandCallback(ChatRoomMediator mediator, AdminService adminService) {
        super(mediator);
        _adminService = adminService;
    }

    
    @Override
    public void commandReceived(String from, String parameters, boolean admin) {
        final String playerName = parameters.strip();
        if (playerName.length() >= 2 && playerName.length() <= MAX_PLAYER_ID_LENGTH) {
            String userString = ChatStrings.user(playerName);
            if (_adminService.removeIgnoredUser(from, playerName)) {
                sendChatMessage(ChatStrings.SYSTEM_USER_ID, from,
                        userString + " removed from ignore list");
            } else {
                sendChatMessage(ChatStrings.SYSTEM_USER_ID, from,
                        userString + " wasn't on your ignore list. Try ignoring them first.");
            }
        } else {
            sendChatMessage(ChatStrings.SYSTEM_USER_ID, from, playerName + " is not a valid username");
        }
    }

}