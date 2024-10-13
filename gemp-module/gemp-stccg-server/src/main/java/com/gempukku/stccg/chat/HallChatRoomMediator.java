package com.gempukku.stccg.chat;

import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.database.IgnoreDAO;
import com.gempukku.stccg.service.AdminService;

import java.util.*;

public class HallChatRoomMediator extends ChatRoomMediator {
    private static final String SYSTEM_USER_ID = "System";
    private static final int MAX_PLAYER_ID_LENGTH = 30;
    private static final String _name = "Game Hall";
    private final ChatServer _chatServer;
    private final AdminService _adminService;
    private final IgnoreDAO _ignoreDAO;

    public HallChatRoomMediator(IgnoreDAO ignoreDAO, ServerObjects objects, int secondsTimeoutPeriod) {
        super(ignoreDAO, objects.getPlayerDAO(), true, secondsTimeoutPeriod, true,
                "You're now in the Game Hall, use /help to get a list of available commands.<br>" +
                        "Don't forget to check out the new Discord chat integration! " +
                        "Click the 'Switch to Discord' button in the lower right ---->");
        _chatServer = objects.getChatServer();
        _adminService = objects.getAdminService();
        _ignoreDAO = objects.getIgnoreDAO();
        try {
            sendMessage(SYSTEM_USER_ID, "Welcome to room: " + _name, true);
        } catch (PrivateInformationException exp) {
            // Ignore, sent as admin
        } catch (ChatCommandErrorException e) {
            // Ignore, no command
        }
        addCallbacks();
    }

    public void initialize() {
        _chatServer.addChatRoom(this, _name);
    }
    
    private void addCallbacks() {
        addChatCommandCallback("ban",
                (from, parameters, admin) -> {
                    if (admin) {
                        String userId = parameters.strip();
                        _adminService.banUser(userId);
                    } else {
                        throw new ChatCommandErrorException("Only administrator can ban users");
                    }
                });
        addChatCommandCallback("banIp",
                (from, parameters, admin) -> {
                    if (admin) {
                        String userId = parameters.strip();
                        _adminService.banIp(userId);
                    } else {
                        throw new ChatCommandErrorException("Only administrator can ban users");
                    }
                });
        addChatCommandCallback("banIpRange",
                (from, parameters, admin) -> {
                    if (admin) {
                        String userId = parameters.strip();
                        _adminService.banIpPrefix(userId);
                    } else {
                        throw new ChatCommandErrorException("Only administrator can ban users");
                    }
                });
        addChatCommandCallback("ignore",
                (from, parameters, admin) -> {
                    final String playerName = parameters.strip();
                    final boolean isFromPlayer = Objects.equals(from, playerName);
                    if (playerName.length() >= 2 && playerName.length() <= 30) {
                        if (!isFromPlayer && _ignoreDAO.addIgnoredUser(from, playerName)) {
                            sendToUser(SYSTEM_USER_ID, from, "User " + playerName + " added to ignore list");
                        } else if (isFromPlayer) {
                            for (String message : getIgnoredUserMessages())
                                sendToUser(from, from, message);
                        } else {
                            sendToUser(SYSTEM_USER_ID, from, "User " + playerName +
                                    " is already on your ignore list");
                        }
                    } else {
                        sendToUser(SYSTEM_USER_ID, from, playerName + " is not a valid username");
                    }
                });
        addChatCommandCallback("unignore",
                (from, parameters, admin) -> {
                    final String playerName = parameters.strip();
                    if (playerName.length() >= 2 && playerName.length() <= MAX_PLAYER_ID_LENGTH) {
                        if (_ignoreDAO.removeIgnoredUser(from, playerName)) {
                            sendToUser(SYSTEM_USER_ID, from, "User " + playerName + " removed from ignore list");
                        } else {
                            sendToUser(SYSTEM_USER_ID, from, "User " + playerName +
                                    " wasn't on your ignore list. Try ignoring them first.");
                        }
                    } else {
                        sendToUser(SYSTEM_USER_ID, from, playerName + " is not a valid username");
                    }
                });
        addChatCommandCallback("listIgnores",
                (from, parameters, admin) -> {
                    final Set<String> ignoredUsers = _ignoreDAO.getIgnoredUsers(from);
                    String ignoredUsersArray = Arrays.toString(ignoredUsers.toArray(new String[0]));
                    sendToUser(SYSTEM_USER_ID, from, "Your ignores: " + ignoredUsersArray);
                });
        addChatCommandCallback("incognito",
                (from, parameters, admin) -> {
                    setIncognito(from, true);
                    sendToUser(SYSTEM_USER_ID, from, "You are now incognito (do not appear in user list)");
                });
        addChatCommandCallback("endIncognito",
                (from, parameters, admin) -> {
                    setIncognito(from, false);
                    sendToUser(SYSTEM_USER_ID, from, "You are no longer incognito");
                });
        addChatCommandCallback("help",
                (from, parameters, admin) -> {
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

                    sendToUser(SYSTEM_USER_ID, from, message.replace("\n", "<br />"));
                });
        addChatCommandCallback("noCommand",
                (from, parameters, admin) -> sendToUser(
                        SYSTEM_USER_ID, from, "\"" + parameters + "\" is not a recognized command."
                ));
        
    }

    @SuppressWarnings("SpellCheckingInspection")
    /* Despite a thorough culling of most Lord of the Rings content in this code base for the Star Trek implementation,
        this is delightful and I never want to lose it. */
    private static List<String> getIgnoredUserMessages() {
        List<String> result = new LinkedList<>();
        result.add("You don't have any friends. Nobody likes you.");
        result.add("Not listening. Not listening!");
        result.add("You're a liar and a thief.");
        result.add("Nope.");
        result.add("Murderer!");
        result.add("Go away. Go away!");
        result.add("Hahahaha!");
        result.add("I hate you, I hate you.");
        result.add("Where would you be without me? Gollum, Gollum. I saved us. It was me. We survived because of me!");
        result.add("Not anymore.");
        result.add("What did you say?");
        result.add("Master looks after us now. We don't need you.");
        result.add("What?");
        result.add("Leave now and never come back.");
        result.add("No!");
        result.add("Leave now and never come back!");
        result.add("Argh!");
        result.add("Leave NOW and NEVER COME BACK!");
        result.add("...");
        result.add("We... We told him to go away! And away he goes, preciouss! Gone, gone, gone! Smeagol is free!");
        return result;
    }

    private void addChatCommandCallback(String command, ChatCommandCallback callback) {
        String commandLower = command.toLowerCase(Locale.ROOT);
        _chatCommandCallbacks.put(commandLower, callback);
    }

}