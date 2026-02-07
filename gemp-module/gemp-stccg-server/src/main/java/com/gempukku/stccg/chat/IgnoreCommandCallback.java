package com.gempukku.stccg.chat;

import com.gempukku.stccg.service.AdminService;

import java.util.Objects;

public class IgnoreCommandCallback extends ChatCommandWithMessage {

    private final AdminService _adminService;
    private static final int MAX_PLAYER_ID_LENGTH = 30;

    IgnoreCommandCallback(ChatRoomMediator mediator, AdminService adminService) {
        super(mediator);
        _adminService = adminService;
    }


    @Override
    public void commandReceived(String from, String parameters, boolean admin) {
        final String playerName = parameters.strip();
        final boolean isFromPlayer = Objects.equals(from, playerName);

        if (playerName.length() >= 2 && playerName.length() <= MAX_PLAYER_ID_LENGTH) {
            String userString = ChatStrings.user(playerName);
            if (!isFromPlayer && _adminService.addIgnoredUser(from, playerName)) {
                sendChatMessage(ChatStrings.SYSTEM_USER_ID, from, userString + " added to ignore list");
            } else if (isFromPlayer) {
                for (String message : ChatStrings.getIgnoredUserMessages())
                    sendChatMessage(from, from, message);
            } else {
                sendChatMessage(ChatStrings.SYSTEM_USER_ID, from,
                        userString + " is already on your ignore list");
            }
        } else {
            sendChatMessage(ChatStrings.SYSTEM_USER_ID, from, playerName + " is not a valid username");
        }
    }

}