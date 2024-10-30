package com.gempukku.stccg.chat;

import com.gempukku.stccg.SubscriptionExpiredException;
import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.database.IgnoreDAO;
import com.gempukku.stccg.database.PlayerDAO;
import com.gempukku.stccg.database.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ChatRoomMediator {
    private final IgnoreDAO _ignoreDAO;
    private final PlayerDAO _playerDAO;
    private static final Logger LOGGER = LogManager.getLogger(ChatRoomMediator.class);
    private final ChatRoom _chatRoom;

    private final Map<String, ChatCommunicationChannel> _listeners = new HashMap<>();

    private final int _channelInactivityTimeoutPeriod;
    private final Set<String> _allowedPlayers;

    private final ReadWriteLock _lock = new ReentrantReadWriteLock();

    final Map<String, ChatCommandCallback> _chatCommandCallbacks = new HashMap<>();
    private String _welcomeMessage;

    public ChatRoomMediator(ServerObjects objects, boolean muteJoinPartMessages,
                            int secondsTimeoutPeriod, boolean allowIncognito, String welcomeMessage) {
        this(objects, muteJoinPartMessages, secondsTimeoutPeriod, null, allowIncognito);
        _welcomeMessage = welcomeMessage;
    }

    public ChatRoomMediator(ServerObjects serverObjects, boolean muteJoinPartMessages, int secondsTimeoutPeriod,
                            Set<String> allowedPlayers, boolean allowIncognito) {
        _ignoreDAO = serverObjects.getIgnoreDAO();
        _playerDAO = serverObjects.getPlayerDAO();
        _allowedPlayers = allowedPlayers;
        _channelInactivityTimeoutPeriod = 1000 * secondsTimeoutPeriod;
        _chatRoom = new ChatRoom(muteJoinPartMessages, allowIncognito);
    }


    public final List<ChatMessage> joinUser(String playerId, boolean admin)
            throws PrivateInformationException, SQLException {
        _lock.writeLock().lock();
        try {
            if (admin || _allowedPlayers == null || _allowedPlayers.contains(playerId)) {
                Set<String> usersToIgnore = _playerDAO.getBannedUsernames();
                Set<String> ignoredUsers = _ignoreDAO.getIgnoredUsers(playerId);
                usersToIgnore.addAll(ignoredUsers);
                ChatCommunicationChannel value = new ChatCommunicationChannel(usersToIgnore);
                _listeners.put(playerId, value);
                _chatRoom.joinChatRoom(playerId, value);
                final List<ChatMessage> chatMessages = value.consumeMessages();
                if (_welcomeMessage != null) {
                    ChatMessage message = new ChatMessage(ChatStrings.SYSTEM_USER_ID, _welcomeMessage, false);
                    chatMessages.add(message);
                }
                return chatMessages;
            } else {
                throw new PrivateInformationException();
            }
        } finally {
            _lock.writeLock().unlock();
        }
    }

    public final ChatCommunicationChannel getChatRoomListener(String playerId) throws SubscriptionExpiredException {
        _lock.readLock().lock();
        try {
            ChatCommunicationChannel chatListener = _listeners.get(playerId);
            if (chatListener == null)
                throw new SubscriptionExpiredException();
            return chatListener;
        } finally {
            _lock.readLock().unlock();
        }
    }

    public final ChatCommunicationChannel getChatRoomListener(User user) throws SubscriptionExpiredException {
        String userName = user.getName();
        return getChatRoomListener(userName);
    }

    public final void sendMessage(String playerId, String message, boolean admin)
            throws PrivateInformationException, ChatCommandErrorException {
        if (message.trim().startsWith("/")) {
            processIfKnownCommand(playerId, message.trim().substring(1), admin);
            return;
        }

        _lock.writeLock().lock();
        try {
            if (!admin && _allowedPlayers != null && !_allowedPlayers.contains(playerId))
                throw new PrivateInformationException();

            LOGGER.trace("{}: {}", playerId, message);
            _chatRoom.postMessage(playerId, message, admin);
        } finally {
            _lock.writeLock().unlock();
        }
    }

    public final void sendMessage(User user, String message)
            throws PrivateInformationException, ChatCommandErrorException {
        String userName = user.getName();
        boolean admin = user.hasType(User.Type.ADMIN);
        sendMessage(userName, message, admin);
    }


    final void setIncognito(String username, boolean incognito) {
        _lock.writeLock().lock();
        try {
            _chatRoom.setUserIncognitoMode(username, incognito);
        } finally {
            _lock.writeLock().unlock();
        }
    }

    final void sendToUser(String from, String to, String message) {
        _lock.writeLock().lock();
        try {
            _chatRoom.postToUser(from, to, message);
        } finally {
            _lock.writeLock().unlock();
        }
    }

    private void processIfKnownCommand(String playerId, String commandString, boolean admin)
            throws ChatCommandErrorException {
        int spaceIndex = commandString.indexOf(' ');
        String commandName;
        String commandParameters="";
        if (spaceIndex>-1) {
            commandName = commandString.substring(0, spaceIndex);
            commandParameters = commandString.substring(spaceIndex+1);
        } else {
            commandName = commandString;
        }
        final ChatCommandCallback callbackForCommand =
                _chatCommandCallbacks.get(commandName.toLowerCase(Locale.ROOT));
        if (callbackForCommand != null) {
            callbackForCommand.commandReceived(playerId, commandParameters, admin);
        } else {
            ChatCommandCallback callbackForNoCommand = _chatCommandCallbacks.get("noCommand");
            callbackForNoCommand.commandReceived(playerId, commandString, false);
        }
    }

    public final void cleanup() {
        _lock.writeLock().lock();
        try {
            long currentTime = System.currentTimeMillis();
            Map<String, ChatCommunicationChannel> copy = new HashMap<>(_listeners);
            for (Map.Entry<String, ChatCommunicationChannel> playerListener : copy.entrySet()) {
                String playerId = playerListener.getKey();
                ChatCommunicationChannel listener = playerListener.getValue();
                if (currentTime > listener.getLastAccessed() + _channelInactivityTimeoutPeriod) {
                    _chatRoom.partChatRoom(playerId);
                    _listeners.remove(playerId);
                }
            }
        } finally {
            _lock.writeLock().unlock();
        }
    }

    public final Collection<String> getUsersInRoom(boolean admin) {
        _lock.readLock().lock();
        try {
            return _chatRoom.getUsersInRoom(admin);
        } finally {
            _lock.readLock().unlock();
        }
    }
}