package com.gempukku.stccg.chat;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.service.AdminService;

import java.util.Set;

public class ListIgnoresCommandCallback extends ChatCommandWithMessage {

    private final AdminService _adminService;

    ListIgnoresCommandCallback(ChatRoomMediator mediator, AdminService adminService) {
        super(mediator);
        _adminService = adminService;
    }

    @Override
    public void commandReceived(String from, String parameters, boolean admin) {
        final Set<String> ignoredUsers = _adminService.getIgnoredUsers(from);
        String ignoredUsersString = TextUtils.concatenateStrings(ignoredUsers);
        sendChatMessage(ChatStrings.SYSTEM_USER_ID, from, "Your ignores: " + ignoredUsersString);
    }

}