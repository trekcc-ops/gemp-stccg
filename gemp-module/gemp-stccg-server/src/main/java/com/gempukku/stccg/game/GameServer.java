package com.gempukku.stccg.game;

import com.gempukku.stccg.AbstractServer;
import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.common.CloseableReadLock;
import com.gempukku.stccg.common.CloseableWriteLock;
import com.gempukku.stccg.hall.GameCreationListener;
import com.gempukku.stccg.hall.GameTable;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GameServer extends AbstractServer {

    private final Map<String, CardGameMediator> _runningGames = new ConcurrentHashMap<>();
    private final List<GameCreationListener> _gameCreationListeners;
    private final CloseableReadLock _readLock;
    private final CloseableWriteLock _writeLock;
    private final CardBlueprintLibrary _cardBlueprintLibrary;

    public GameServer(CardBlueprintLibrary cardBlueprintLibrary, List<GameCreationListener> gameCreationListeners) {
        _cardBlueprintLibrary = cardBlueprintLibrary;
        _gameCreationListeners = gameCreationListeners;
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

    public final void createNewGame(String tournamentName, GameTable gameTable,
                                    List<GameResultListener> resultListeners) {
        try (CloseableWriteLock ignored = _writeLock.open()) {
            CardGameMediator mediator = gameTable.createMediator(_cardBlueprintLibrary, tournamentName,
                    _gameCreationListeners, resultListeners);
            addMediatorToRunningGames(mediator);
        }
    }

    private void addMediatorToRunningGames(CardGameMediator mediator) {
        _runningGames.put(mediator.getGameId(), mediator);
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