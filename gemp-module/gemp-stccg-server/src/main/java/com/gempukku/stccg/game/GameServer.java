package com.gempukku.stccg.game;

import com.gempukku.stccg.AbstractServer;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.chat.*;
import com.gempukku.stccg.common.CloseableReadLock;
import com.gempukku.stccg.common.CloseableWriteLock;
import com.gempukku.stccg.hall.GameSettings;
import com.gempukku.stccg.hall.GameTable;

import java.net.HttpURLConnection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GameServer extends AbstractServer {

    private static final long MILLIS_TO_MINUTES = 1000 * 60;
    private final Map<String, CardGameMediator> _runningGames = new ConcurrentHashMap<>();
    private final Collection<String> _gameDeathWarningsSent = new HashSet<>();
    private final Map<String, Date> _finishedGamesTime = Collections.synchronizedMap(new LinkedHashMap<>());
    private final ChatServer _chatServer;
    private final GameHistoryService _historyService;
    private final CloseableReadLock _readLock;
    private final CloseableWriteLock _writeLock;
    private final CardBlueprintLibrary _cardBlueprintLibrary;

    public GameServer(ChatServer chatServer, GameHistoryService historyService,
                      CardBlueprintLibrary cardBlueprintLibrary) {
        _chatServer = chatServer;
        _historyService = historyService;
        _cardBlueprintLibrary = cardBlueprintLibrary;
        ReadWriteLock lock = new ReentrantReadWriteLock();
        _readLock = new CloseableReadLock(lock);
        _writeLock = new CloseableWriteLock(lock);
    }

    protected final void cleanup() {
        try (CloseableWriteLock ignored = _writeLock.open()) {
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
        }
    }

    private static String getChatRoomName(String gameId) {
        return "Game" + gameId;
    }

    public final void createNewGame(String tournamentName, GameTable gameTable, List<GameResultListener> listeners) {
        try (CloseableWriteLock ignored = _writeLock.open()) {
            GameParticipant[] participants = gameTable.getPlayers().toArray(new GameParticipant[0]);
            if (participants.length < 2)
                throw new IllegalArgumentException("There has to be at least two players");
            GameSettings gameSettings = gameTable.getGameSettings();
            CardGameMediator cardGameMediator = new CardGameMediator(participants, _cardBlueprintLibrary,
                    gameSettings.allowsSpectators(), gameSettings.getTimeSettings(), gameSettings.getGameFormat(),
                    gameSettings.getGameType());
            listeners.add(new FinishedGamesResultListener(this, cardGameMediator.getGameId()));
            _chatServer.createGameChatRoom(gameSettings, participants, cardGameMediator.getGameId());
            cardGameMediator.initialize(_historyService, tournamentName, listeners);
            _runningGames.put(cardGameMediator.getGameId(), cardGameMediator);
            gameTable.startGame(cardGameMediator);
        }
    }


    public final CardGameMediator getGameById(String gameId) throws HttpProcessingException {
        try (CloseableReadLock ignored = _readLock.open()) {
            CardGameMediator mediator = _runningGames.get(gameId);
            if (mediator == null)
                throw new HttpProcessingException(HttpURLConnection.HTTP_NOT_FOUND); // 404
            else return mediator;
        }
    }

    public void logGameEndTime(String gameId) {
        _finishedGamesTime.put(gameId, new Date());
    }
}