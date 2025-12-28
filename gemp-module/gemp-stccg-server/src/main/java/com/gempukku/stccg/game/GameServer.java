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

    private final Map<String, CardGameMediator> _runningGames = new ConcurrentHashMap<>();
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
            for (CardGameMediator mediator : _runningGames.values()) {
                mediator.cleanup();
            }
            _runningGames.values().removeIf(CardGameMediator::isDestroyed);
        }
    }

    public final void createNewGame(String tournamentName, GameTable gameTable, List<GameResultListener> listeners) {
        try (CloseableWriteLock ignored = _writeLock.open()) {
            GameParticipant[] participants = gameTable.getPlayers().toArray(new GameParticipant[0]);
            if (participants.length < 2)
                throw new IllegalArgumentException("There has to be at least two players");
            GameSettings gameSettings = gameTable.getGameSettings();
            CardGameMediator cardGameMediator = new CardGameMediator(participants, _cardBlueprintLibrary,
                    gameSettings.allowsSpectators(), gameSettings.getTimeSettings(), gameSettings.getGameFormat(),
                    gameSettings.getGameType(), _chatServer, gameSettings.isCompetitive());
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

}