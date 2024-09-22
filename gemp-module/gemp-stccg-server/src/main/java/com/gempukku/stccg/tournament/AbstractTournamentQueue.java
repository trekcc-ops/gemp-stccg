package com.gempukku.stccg.tournament;

import com.gempukku.stccg.cards.CardDeck;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.db.vo.CollectionType;
import com.gempukku.stccg.db.User;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public abstract class AbstractTournamentQueue implements TournamentQueue {
    protected final int _cost;
    protected final Queue<String> _players = new LinkedList<>();
    protected final Map<String, CardDeck> _playerDecks = new HashMap<>();
    protected final boolean _requiresDeck;

    private final CollectionType _currencyCollection = CollectionType.MY_CARDS;

    protected final PairingMechanism _pairingMechanism;
    protected final CollectionType _collectionType;
    protected final TournamentPrizes _tournamentPrizes;
    protected final String _format;

    public AbstractTournamentQueue(int cost, boolean requiresDeck, CollectionType collectionType, TournamentPrizes tournamentPrizes, PairingMechanism pairingMechanism, String format) {
        _cost = cost;
        _requiresDeck = requiresDeck;
        _collectionType = collectionType;
        _tournamentPrizes = tournamentPrizes;
        _pairingMechanism = pairingMechanism;
        _format = format;
    }

    @Override
    public String getPairingDescription() {
        return _pairingMechanism.getPlayOffSystem();
    }

    @Override
    public final CollectionType getCollectionType() {
        return _collectionType;
    }

    @Override
    public final String getPrizesDescription() {
        return _tournamentPrizes.getPrizeDescription();
    }

    @Override
    public final synchronized void joinPlayer(CollectionsManager collectionsManager, User player, CardDeck deck) throws SQLException, IOException {
        if (!_players.contains(player.getName()) && isJoinable()) {
            if (_cost <= 0 || collectionsManager.removeCurrencyFromPlayerCollection("Joined "+getTournamentQueueName()+" queue", player, _currencyCollection, _cost)) {
                _players.add(player.getName());
                if (_requiresDeck)
                    _playerDecks.put(player.getName(), deck);
            }
        }
    }

    @Override
    public final synchronized void leavePlayer(CollectionsManager collectionsManager, User player) throws SQLException, IOException {
        if (_players.contains(player.getName())) {
            if (_cost > 0)
                collectionsManager.addCurrencyToPlayerCollection(true, "Return for leaving "+getTournamentQueueName()+" queue", player, _currencyCollection, _cost);
            _players.remove(player.getName());
            _playerDecks.remove(player.getName());
        }
    }

    @Override
    public final synchronized void leaveAllPlayers(CollectionsManager collectionsManager) throws SQLException, IOException {
        if (_cost > 0) {
            for (String player : _players)
                collectionsManager.addCurrencyToPlayerCollection(false, "Return for leaving "+getTournamentQueueName()+" queue", player, _currencyCollection, _cost);
        }
        _players.clear();
        _playerDecks.clear();
    }

    @Override
    public final synchronized int getPlayerCount() {
        return _players.size();
    }

    @Override
    public final synchronized boolean isPlayerSignedUp(String player) {
        return _players.contains(player);
    }

    @Override
    public final int getCost() {
        return _cost;
    }

    @Override
    public final boolean isRequiresDeck() {
        return _requiresDeck;
    }

    @Override
    public final String getFormat() {
        return _format;
    }
}
