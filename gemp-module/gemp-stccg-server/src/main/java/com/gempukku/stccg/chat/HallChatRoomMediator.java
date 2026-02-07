package com.gempukku.stccg.chat;

import com.gempukku.stccg.async.handler.HTMLUtils;
import com.gempukku.stccg.service.AdminService;

import java.util.Locale;

public class HallChatRoomMediator extends ChatRoomMediator {
    private final AdminService _adminService;

    private static final int HALL_TIMEOUT_PERIOD = 300;

    private enum ChatCommandType {
        ban, banIp, banIpRange, endIncognito, help, ignore, incognito, listIgnores, noCommand, unignore
    }

    public HallChatRoomMediator(AdminService adminService) {
        super(true, HALL_TIMEOUT_PERIOD, true, HTMLUtils.HALL_WELCOME_MESSAGE,
                "Game Hall");
        _adminService = adminService;
        try {
            sendChatMessage(ChatStrings.SYSTEM_USER_ID, "Welcome to room: " + _roomName, true);
        } catch (PrivateInformationException exp) {
            // Ignore, sent as admin
        } catch (ChatCommandErrorException e) {
            // Ignore, no command
        }
        addCallbacks();
    }
    private void addCallbacks() {
        addChatCommandCallback(ChatCommandType.ban, new BanUserCommandCallback(_adminService));
        addChatCommandCallback(ChatCommandType.banIp, new BanIpCommandCallback(_adminService));
        addChatCommandCallback(ChatCommandType.banIpRange, new BanIpRangeCommandCallback(_adminService));
        addChatCommandCallback(ChatCommandType.ignore,
                new IgnoreCommandCallback(this, _adminService));
        addChatCommandCallback(ChatCommandType.unignore,
                new UnignoreCommandCallback(this, _adminService));
        addChatCommandCallback(ChatCommandType.listIgnores,
                new ListIgnoresCommandCallback(this, _adminService));
        addChatCommandCallback(ChatCommandType.incognito, 
                new IncognitoCommandCallback(this, true));
        addChatCommandCallback(ChatCommandType.endIncognito, 
                new IncognitoCommandCallback(this, false));
        addChatCommandCallback(ChatCommandType.help, new HelpCommandCallback(this));
        addChatCommandCallback(ChatCommandType.noCommand,
                (from, parameters, admin) -> sendToUser(
                        ChatStrings.SYSTEM_USER_ID, from, "\"" + parameters + "\" is not a recognized command."
                ));
        
    }

    private void addChatCommandCallback(ChatCommandType command, ChatCommandCallback callback) {
        String commandLower = command.name().toLowerCase(Locale.ROOT);
        _chatCommandCallbacks.put(commandLower, callback);
    }

}