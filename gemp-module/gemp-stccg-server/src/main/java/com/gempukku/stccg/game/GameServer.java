package com.gempukku.stccg.game;

import com.gempukku.stccg.AbstractServer;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.chat.*;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.hall.GameSettings;
import com.gempukku.stccg.hall.GameTable;

import java.net.HttpURLConnection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GameServer extends AbstractServer {

    private static final long MILLIS_TO_MINUTES = 1000 * 60;
    private static final int TIMEOUT_PERIOD = 30;

    private final Map<String, CardGameMediator> _runningGames = new ConcurrentHashMap<>();
    private final Collection<String> _gameDeathWarningsSent = new HashSet<>();

    private final Map<String, Date> _finishedGamesTime = Collections.synchronizedMap(new LinkedHashMap<>());

    private int _nextGameId = 1;

    private final ChatServer _chatServer;
    private final GameRecorder _gameRecorder;

    private final ReadWriteLock _lock = new ReentrantReadWriteLock();

    public GameServer(ChatServer chatServer, GameRecorder gameRecorder) {
        _chatServer = chatServer;
        _gameRecorder = gameRecorder;
    }

    protected final void cleanup() {
        _lock.writeLock().lock();
        try {
            long currentTime = System.currentTimeMillis();

            Map<String, Date> copy = new LinkedHashMap<>(_finishedGamesTime);
            for (Map.Entry<String, Date> finishedGame : copy.entrySet()) {
                String gameId = finishedGame.getKey();
                // 4 minutes
                long _timeToGameDeathWarning = MILLIS_TO_MINUTES * 4;
                if (currentTime > finishedGame.getValue().getTime() + _timeToGameDeathWarning
                        && !_gameDeathWarningsSent.contains(gameId)) {
                    try {
                        String message = "This game is already finished and will be shortly removed, " +
                                "please move to the Game Hall";
                        ChatRoomMediator chatRoom = _chatServer.getChatRoom(getChatRoomName(gameId));
                        chatRoom.sendChatMessage(ChatStrings.SYSTEM_USER_ID, message, true);
                    } catch (PrivateInformationException exp) {
                        // Ignore, sent as admin
                    } catch (ChatCommandErrorException e) {
                        // Ignore, no command
                    }
                    _gameDeathWarningsSent.add(gameId);
                }
                // 5 minutes
                long _timeToGameDeath = MILLIS_TO_MINUTES * 5;
                if (currentTime > finishedGame.getValue().getTime() + _timeToGameDeath) {
                    _runningGames.get(gameId).destroy();
                    _gameDeathWarningsSent.remove(gameId);
                    _runningGames.remove(gameId);
                    _chatServer.destroyChatRoom(getChatRoomName(gameId));
                    _finishedGamesTime.remove(gameId);
                } else {
                    break;
                }
            }

            for (CardGameMediator cardGameMediator : _runningGames.values())
                cardGameMediator.cleanup();
        } finally {
            _lock.writeLock().unlock();
        }
    }

    private static String getChatRoomName(String gameId) {
        return "Game" + gameId;
    }

    private void createGameChatRoom(GameSettings gameSettings, GameParticipant[] participants, String gameId) {
        String chatRoomName = getChatRoomName(gameId);
        Set<String> allowedUsers = new HashSet<>();
        boolean isCompetitive = gameSettings.isCompetitive();

        if (isCompetitive) {
            for (GameParticipant participant : participants)
                allowedUsers.add(participant.getPlayerId());
        }
        _chatServer.createChatRoom(chatRoomName, false, allowedUsers, TIMEOUT_PERIOD, isCompetitive);
    }


    public final void createNewGame(String tournamentName, final GameParticipant[] participants,
                                    GameTable gameTable, CardBlueprintLibrary blueprintLibrary,
                                    List<GameResultListener> listeners) {
        _lock.writeLock().lock();
        try {
            if (participants.length < 2)
                throw new IllegalArgumentException("There has to be at least two players");
            final String gameId = String.valueOf(_nextGameId);
            GameSettings gameSettings = gameTable.getGameSettings();
            listeners.add(new FinishedGamesResultListener(this, gameId));
            CardGameMediator cardGameMediator = new CardGameMediator(gameId, participants, blueprintLibrary,
                    gameSettings.allowsSpectators(), gameSettings.getTimeSettings(), gameSettings.getGameFormat(),
                    gameSettings.getGameType());
            createGameChatRoom(gameSettings, participants, gameId);
            cardGameMediator.initialize(_gameRecorder, tournamentName, listeners);
            _runningGames.put(gameId, cardGameMediator);
            _nextGameId++;
            gameTable.startGame(cardGameMediator);
        } finally {
            _lock.writeLock().unlock();
        }
    }


    public final CardGameMediator getGameById(String gameId) throws HttpProcessingException {
        CardGameMediator mediator;
        _lock.readLock().lock();
        try {
            mediator = _runningGames.get(gameId);
        } finally {
            _lock.readLock().unlock();
        }
        if (mediator == null)
            throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
        else return mediator;
    }

    public void cancelGame(User user, String gameId) throws HttpProcessingException {
        CardGameMediator gameMediator = getGameById(gameId);
        gameMediator.cancel(user.getName());
    }

    public void setPlayerAutoPassSettings(User resourceOwner, String gameId, Set<Phase> autoPassPhases)
            throws HttpProcessingException {
        CardGameMediator gameMediator = getGameById(gameId);
        gameMediator.setPlayerAutoPassSettings(resourceOwner.getName(), autoPassPhases);
    }

    public void logGameEndTime(String gameId) {
        _finishedGamesTime.put(gameId, new Date());
    }
}