package com.gempukku.stccg.chat;

import java.util.Set;

public class GameChatRoomMediator extends ChatRoomMediator {

    private static final int TIMEOUT_PERIOD = 30;

    private boolean _destroyWarningSent;

    public GameChatRoomMediator(boolean muteJoinPartMessages, Set<String> allowedPlayers,
                                boolean allowIncognito, String roomName) {
        super(muteJoinPartMessages, TIMEOUT_PERIOD, allowedPlayers, allowIncognito, roomName);
    }

    public void sendChatDestroyWarning() {
        if (!_destroyWarningSent) {
            _destroyWarningSent = true;
            try {
                String message = "This game is already finished and will be shortly removed, " +
                        "please move to the Game Hall";
                sendChatMessage(ChatStrings.SYSTEM_USER_ID, message, true);
            } catch (PrivateInformationException exp) {
                // Ignore, sent as admin
            } catch (ChatCommandErrorException e) {
                // Ignore, no command
            }
        }
    }
}