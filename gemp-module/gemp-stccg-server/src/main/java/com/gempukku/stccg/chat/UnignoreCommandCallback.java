package com.gempukku.stccg.chat;

import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.database.IgnoreDAO;

public class UnignoreCommandCallback extends ChatCommandWithMessage {
    
    private final IgnoreDAO _ignoreDAO;
    private static final int MAX_PLAYER_ID_LENGTH = 30;


    UnignoreCommandCallback(ChatRoomMediator mediator, ServerObjects serverObjects) {
        super(mediator);
        _ignoreDAO = serverObjects.getIgnoreDAO();
    }

    
    @Override
    public void commandReceived(String from, String parameters, boolean admin) {
        final String playerName = parameters.strip();
        if (playerName.length() >= 2 && playerName.length() <= MAX_PLAYER_ID_LENGTH) {
            String userString = ChatStrings.user(playerName);
            if (_ignoreDAO.removeIgnoredUser(from, playerName)) {
                sendMessage(ChatStrings.SYSTEM_USER_ID, from,
                        userString + " removed from ignore list");
            } else {
                sendMessage(ChatStrings.SYSTEM_USER_ID, from,
                        userString + " wasn't on your ignore list. Try ignoring them first.");
            }
        } else {
            sendMessage(ChatStrings.SYSTEM_USER_ID, from, playerName + " is not a valid username");
        }
    }

}