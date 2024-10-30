package com.gempukku.stccg.chat;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.database.IgnoreDAO;

import java.util.Set;

public class ListIgnoresCommandCallback extends ChatCommandWithMessage {

    private final IgnoreDAO _ignoreDAO;

    ListIgnoresCommandCallback(ChatRoomMediator mediator, ServerObjects serverObjects) {
        super(mediator);
        _ignoreDAO = serverObjects.getIgnoreDAO();
    }

    @Override
    public void commandReceived(String from, String parameters, boolean admin) {
        final Set<String> ignoredUsers = _ignoreDAO.getIgnoredUsers(from);
        String ignoredUsersString = TextUtils.concatenateStrings(ignoredUsers);
        sendMessage(ChatStrings.SYSTEM_USER_ID, from, "Your ignores: " + ignoredUsersString);
    }

}