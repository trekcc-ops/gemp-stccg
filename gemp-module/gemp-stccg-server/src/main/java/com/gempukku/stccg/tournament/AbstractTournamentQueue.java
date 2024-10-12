package com.gempukku.stccg.tournament;

import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.collection.CollectionsManager;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.db.User;
import com.gempukku.stccg.collection.CollectionType;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public abstract class AbstractTournamentQueue implements TournamentQueue {
    private final int _cost;
    final Queue<String> _players = new LinkedList<>();
    final Map<String, CardDeck> _playerDecks = new HashMap<>();
    private final boolean _requiresDeck;

    private final CollectionType _currencyCollection = CollectionType.MY_CARDS;

    final PairingMechanism _pairingMechanism;
    final CollectionType _collectionType;
    final TournamentPrizes _tournamentPrizes;
    final String _format;
    final TournamentService _tournamentService;

    AbstractTournamentQueue(int cost, boolean requiresDeck, CollectionType collectionType,
                            TournamentPrizes tournamentPrizes, PairingMechanism pairingMechanism,
                            String format, TournamentService tournamentService) {
        _cost = cost;
        _requiresDeck = requiresDeck;
        _collectionType = collectionType;
        _tournamentPrizes = tournamentPrizes;
        _pairingMechanism = pairingMechanism;
        _format = format;
        _tournamentService = tournamentService;
    }

    AbstractTournamentQueue(TournamentQueueInfo queueInfo, ServerObjects objects) {
        _cost = queueInfo.getCost();
        _requiresDeck = true;
        _collectionType = CollectionType.ALL_CARDS;
        CardBlueprintLibrary library = objects.getCardBlueprintLibrary();
        _tournamentPrizes = queueInfo.getPrizes(library);
        _pairingMechanism = queueInfo.getPairingMechanism();
        _format = queueInfo.getFormat();
        _tournamentService = objects.getTournamentService();
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
    public final synchronized void joinPlayer(CollectionsManager collectionsManager, User player, CardDeck deck)
            throws SQLException, IOException {
        String playerName = player.getName();
        if (!_players.contains(playerName) && isJoinable()) {
            String tournamentQueueName = getTournamentQueueName();
            if (_cost <= 0 || collectionsManager.removeCurrencyFromPlayerCollection(
                    "Joined " + tournamentQueueName + " queue", player, _currencyCollection, _cost)) {
                _players.add(playerName);
                if (_requiresDeck)
                    _playerDecks.put(playerName, deck);
            }
        }
    }

    @Override
    public final synchronized void leavePlayer(CollectionsManager collectionsManager, User player)
            throws SQLException, IOException {
        String playerName = player.getName();
        if (_players.contains(playerName)) {
            if (_cost > 0)
                refundCurrencyForPlayer(collectionsManager, player);
            _players.remove(playerName);
            _playerDecks.remove(playerName);
        }
    }

    @Override
    public final synchronized void leaveAllPlayers(CollectionsManager collectionsManager)
            throws SQLException, IOException {
        if (_cost > 0) {
            for (String player : _players)
                refundCurrencyForPlayer(collectionsManager, player);
        }
        _players.clear();
        _playerDecks.clear();
    }

    private void refundCurrencyForPlayer(CollectionsManager collectionsManager, String player)
            throws SQLException, IOException {
        String tournamentQueueName = getTournamentQueueName();
        collectionsManager.addCurrencyToPlayerCollection(
                false, "Return for leaving " + tournamentQueueName + " queue",
                player, _currencyCollection, _cost
        );
    }

    private void refundCurrencyForPlayer(CollectionsManager collectionsManager, User user)
            throws SQLException, IOException {
        String tournamentQueueName = getTournamentQueueName();
        collectionsManager.addCurrencyToPlayerCollection(
                false, "Return for leaving " + tournamentQueueName + " queue",
                user, _currencyCollection, _cost
        );
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