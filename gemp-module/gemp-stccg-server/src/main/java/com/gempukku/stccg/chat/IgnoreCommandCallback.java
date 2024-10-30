package com.gempukku.stccg.chat;

import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.database.IgnoreDAO;

import java.util.Objects;

public class IgnoreCommandCallback extends ChatCommandWithMessage {

    private final IgnoreDAO _ignoreDAO;
    private static final int MAX_PLAYER_ID_LENGTH = 30;

    IgnoreCommandCallback(ChatRoomMediator mediator, ServerObjects serverObjects) {
        super(mediator);
        _ignoreDAO = serverObjects.getIgnoreDAO();
    }


    @Override
    public void commandReceived(String from, String parameters, boolean admin) {
        final String playerName = parameters.strip();
        final boolean isFromPlayer = Objects.equals(from, playerName);

        if (playerName.length() >= 2 && playerName.length() <= MAX_PLAYER_ID_LENGTH) {
            String userString = ChatStrings.user(playerName);
            if (!isFromPlayer && _ignoreDAO.addIgnoredUser(from, playerName)) {
                sendMessage(ChatStrings.SYSTEM_USER_ID, from, userString + " added to ignore list");
            } else if (isFromPlayer) {
                for (String message : ChatStrings.getIgnoredUserMessages())
                    sendMessage(from, from, message);
            } else {
                sendMessage(ChatStrings.SYSTEM_USER_ID, from,
                        userString + " is already on your ignore list");
            }
        } else {
            sendMessage(ChatStrings.SYSTEM_USER_ID, from, playerName + " is not a valid username");
        }
    }

}