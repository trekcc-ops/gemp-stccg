package com.gempukku.stccg.chat;

import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.async.handler.HTMLUtils;
import com.gempukku.stccg.service.AdminService;

import java.util.Locale;

public class HallChatRoomMediator extends ChatRoomMediator {
    private final ServerObjects _serverObjects;

    private enum ChatCommandType {
        ban, banIp, banIpRange, endIncognito, help, ignore, incognito, listIgnores, noCommand, unignore
    }

    public HallChatRoomMediator(ServerObjects objects, int secondsTimeoutPeriod) {
        super(objects, true, secondsTimeoutPeriod, true, HTMLUtils.HALL_WELCOME_MESSAGE,
                "Game Hall");
        _serverObjects = objects;
        try {
            sendMessage(ChatStrings.SYSTEM_USER_ID, "Welcome to room: " + _roomName, true);
        } catch (PrivateInformationException exp) {
            // Ignore, sent as admin
        } catch (ChatCommandErrorException e) {
            // Ignore, no command
        }
        addCallbacks();
    }
    private void addCallbacks() {
        AdminService adminService = _serverObjects.getAdminService();
        addChatCommandCallback(ChatCommandType.ban, new BanUserCommandCallback(adminService));
        addChatCommandCallback(ChatCommandType.banIp, new BanIpCommandCallback(adminService));
        addChatCommandCallback(ChatCommandType.banIpRange, new BanIpRangeCommandCallback(adminService));
        addChatCommandCallback(ChatCommandType.ignore,
                new IgnoreCommandCallback(this, _serverObjects));
        addChatCommandCallback(ChatCommandType.unignore,
                new UnignoreCommandCallback(this, _serverObjects));
        addChatCommandCallback(ChatCommandType.listIgnores,
                new ListIgnoresCommandCallback(this, _serverObjects));
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