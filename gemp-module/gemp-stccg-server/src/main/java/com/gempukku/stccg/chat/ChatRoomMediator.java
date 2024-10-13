package com.gempukku.stccg.chat;

import com.gempukku.stccg.SubscriptionExpiredException;
import com.gempukku.stccg.db.IgnoreDAO;
import com.gempukku.stccg.db.PlayerDAO;
import com.gempukku.stccg.db.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ChatRoomMediator {
    private final IgnoreDAO ignoreDAO;
    private final PlayerDAO playerDAO;
    private static final Logger LOGGER = LogManager.getLogger(ChatRoomMediator.class);
    private final ChatRoom _chatRoom;

    private final Map<String, ChatCommunicationChannel> _listeners = new HashMap<>();

    private final int _channelInactivityTimeoutPeriod;
    private final Set<String> _allowedPlayers;

    private final ReadWriteLock _lock = new ReentrantReadWriteLock();

    final Map<String, ChatCommandCallback> _chatCommandCallbacks = new HashMap<>();
    private String welcomeMessage;

    public ChatRoomMediator(IgnoreDAO ignoreDAO, PlayerDAO playerDAO, boolean muteJoinPartMessages,
                            int secondsTimeoutPeriod, boolean allowIncognito, String welcomeMessage) {
        this(ignoreDAO, playerDAO, muteJoinPartMessages, secondsTimeoutPeriod, null, allowIncognito);
        this.welcomeMessage = welcomeMessage;
    }

    public ChatRoomMediator(IgnoreDAO ignoreDAO, PlayerDAO playerDAO, boolean muteJoinPartMessages,
                            int secondsTimeoutPeriod, Set<String> allowedPlayers, boolean allowIncognito) {
        this.ignoreDAO = ignoreDAO;
        this.playerDAO = playerDAO;
        _allowedPlayers = allowedPlayers;
        _channelInactivityTimeoutPeriod = 1000 * secondsTimeoutPeriod;
        _chatRoom = new ChatRoom(muteJoinPartMessages, allowIncognito);
    }

    public final List<ChatMessage> joinUser(String playerId, boolean admin)
            throws PrivateInformationException, SQLException {
        _lock.writeLock().lock();
        try {
            if (!admin && _allowedPlayers != null && !_allowedPlayers.contains(playerId))
                throw new PrivateInformationException();

            Set<String> usersToIgnore = playerDAO.getBannedUsernames();
            usersToIgnore.addAll(ignoreDAO.getIgnoredUsers(playerId));
            ChatCommunicationChannel value = new ChatCommunicationChannel(usersToIgnore);
            _listeners.put(playerId, value);
            _chatRoom.joinChatRoom(playerId, value);
            final List<ChatMessage> chatMessages = value.consumeMessages();
            if (welcomeMessage != null)
                chatMessages.add(new ChatMessage(new Date(), "System", welcomeMessage, false));
            return chatMessages;
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
            _chatRoom.postMessage(playerId, message, true, admin);
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


    public final void setIncognito(String username, boolean incognito) {
        _lock.writeLock().lock();
        try {
            _chatRoom.setUserIncognitoMode(username, incognito);
        } finally {
            _lock.writeLock().unlock();
        }
    }

    public final void sendToUser(String from, String to, String message) {
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
        final ChatCommandCallback callbackForCommand = _chatCommandCallbacks.get(commandName.toLowerCase());
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