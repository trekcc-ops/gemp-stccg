package com.gempukku.stccg.tournament;

import com.gempukku.stccg.collection.CollectionType;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.formats.GameFormat;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public abstract class TournamentQueue {
    protected final int _cost;
    protected final Queue<String> _players = new LinkedList<>();
    protected final Map<String, CardDeck> _playerDecks = new HashMap<>();
    protected final boolean _requiresDeck;

    protected final PairingMechanism _pairingMechanism;
    protected final CollectionType _collectionType;
    protected final TournamentPrizes _tournamentPrizes;
    protected final GameFormat _gameFormat;
    private final Tournament.Stage _stage;
    protected final TournamentService _tournamentService;
    protected final String _queueName;

    public TournamentQueue(int cost, boolean requiresDeck, CollectionType collectionType,
                           TournamentPrizes tournamentPrizes, PairingMechanism pairingMechanism,
                           GameFormat gameFormat, TournamentService tournamentService, Tournament.Stage stage,
                           String queueName) {
        _cost = cost;
        _requiresDeck = requiresDeck;
        _collectionType = collectionType;
        _tournamentPrizes = tournamentPrizes;
        _pairingMechanism = pairingMechanism;
        _gameFormat = gameFormat;
        _tournamentService = tournamentService;
        _stage = stage;
        _queueName = queueName;
    }

    public TournamentQueue(TournamentQueueInfo queueInfo, TournamentPrizes prizes,
                           TournamentService tournamentService, Tournament.Stage stage, String queueName,
                           GameFormat gameFormat) {
        this(queueInfo.getCost(), true, CollectionType.ALL_CARDS, prizes,
                queueInfo.getPairingMechanism(), gameFormat, tournamentService, stage, queueName);
    }



    public String getPairingDescription() {
        return _pairingMechanism.getPlayOffSystem();
    }

    public String getPairingRegistryRepresentation() {
        return _pairingMechanism.getRegistryRepresentation();
    }


    public final CollectionType getCollectionType() {
        return _collectionType;
    }

    public final String getPrizesRegistryRepresentation() {
        return _tournamentPrizes.getRegistryRepresentation();
    }


    public final String getPrizesDescription() {
        return _tournamentPrizes.getPrizeDescription();
    }

    public final synchronized void joinPlayer(CollectionsManager collectionsManager, User player, CardDeck deck)
            throws SQLException, IOException {
        String playerName = player.getName();
        if (!_players.contains(playerName) && isJoinable()) {
            if (_cost <= 0 || collectionsManager.removeCurrencyFromPlayerCollection(
                    "Joined " + getTournamentQueueName() + " queue", player, _cost)) {
                _players.add(playerName);
                if (_requiresDeck)
                    _playerDecks.put(playerName, deck);
            }
        }
    }

    public final synchronized void leavePlayer(CollectionsManager collectionsManager, User player)
            throws SQLException, IOException {
        String playerName = player.getName();
        if (_players.contains(playerName)) {
            if (_cost > 0)
                collectionsManager.addCurrencyToPlayerCollection(true,
                        "Return for leaving " + getTournamentQueueName() + " queue", player, _cost);
            _players.remove(playerName);
            _playerDecks.remove(playerName);
        }
    }

    public final synchronized void leaveAllPlayers(CollectionsManager collectionsManager)
            throws SQLException, IOException {
        if (_cost > 0) {
            for (String player : _players) {
                collectionsManager.addCurrencyToPlayerCollection(false,
                        "Return for leaving " + getTournamentQueueName() + " queue", player, _cost);
            }
        }
        _players.clear();
        _playerDecks.clear();
    }

    public final synchronized int getPlayerCount() {
        return _players.size();
    }

    public final synchronized boolean isPlayerSignedUp(String player) {
        return _players.contains(player);
    }

    public final int getCost() {
        return _cost;
    }

    public final boolean isRequiresDeck() {
        return _requiresDeck;
    }

    public GameFormat getGameFormat() {
        return _gameFormat;
    }

    public String getFormatCode() {
        return _gameFormat.getCode();
    }

    public Tournament.Stage getStage() {
        return _stage;
    }

    public abstract boolean isJoinable();
    public String getTournamentQueueName() {
        return _queueName;
    }
    public abstract String getStartCondition();

    public abstract void process(TournamentQueueCallback callback, CollectionsManager collectionsManager,
                                 TournamentService tournamentService)
            throws SQLException, IOException;

    public abstract boolean shouldBeRemovedFromHall();

}