package com.gempukku.stccg.database;

import com.gempukku.stccg.async.Cached;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.async.LoggingProxy;
import org.apache.commons.collections4.map.LRUMap;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CachedDeckDAO implements DeckDAO, Cached {

    private final static int DECK_LIMIT = 100;
    private final DeckDAO _delegate;
    private final Map<String, Set<Map.Entry<String, String>>> _playerDeckNames =
            Collections.synchronizedMap(new LRUMap<>(DECK_LIMIT));
    private final Map<String, CardDeck> _decks = Collections.synchronizedMap(new LRUMap<>(DECK_LIMIT));

    public CachedDeckDAO(DbAccess dbAccess) {
        _delegate = LoggingProxy.createLoggingProxy(DeckDAO.class, new DbDeckDAO(dbAccess));
    }

    @Override
    public final void clearCache() {
        _playerDeckNames.clear();
        _decks.clear();
    }

    @Override
    public final int getItemCount() {
        return _playerDeckNames.size()+_decks.size();
    }

    private static String constructPlayerDeckNamesKey(User player) {
        return player.getName();
    }
    private static String constructDeckKey(User player, String name) {
        return player.getName()+"-"+name;
    }

    @Override
    public final void deleteDeckForPlayer(User player, String name) {
        _delegate.deleteDeckForPlayer(player, name);
        String key = constructPlayerDeckNamesKey(player);
        _playerDeckNames.remove(key);
    }

    @Override
    public final CardDeck getDeckForPlayer(User player, String name) {
        String key = constructDeckKey(player, name);
        CardDeck deck = _decks.get(key);
        if (deck == null) {
            deck = _delegate.getDeckForPlayer(player, name);
            _decks.put(key, deck);
        }
        return deck;
    }

    @Override
    public final Set<Map.Entry<String, String>> getPlayerDeckNames(User player) {
        String cacheKey = constructPlayerDeckNamesKey(player);
        Set<Map.Entry<String, String>> deckNames = _playerDeckNames.get(cacheKey);
        if (deckNames == null) {
            deckNames = Collections.synchronizedSet(new HashSet<>(_delegate.getPlayerDeckNames(player)));
            _playerDeckNames.put(cacheKey, deckNames);
        }
        return deckNames;
    }

    @Override
    public final CardDeck renameDeck(User player, String oldName, String newName) {
        CardDeck deck = _delegate.renameDeck(player, oldName, newName);
        _playerDeckNames.remove(constructPlayerDeckNamesKey(player));
        _decks.remove(constructDeckKey(player, oldName));
        _decks.put(constructDeckKey(player, newName), deck);

        return deck;
    }

    @Override
    public final void saveDeckForPlayer(User player, String name, String targetFormat, String notes, CardDeck deck) {
        _delegate.saveDeckForPlayer(player, name, targetFormat, notes, deck);
        _playerDeckNames.remove(constructPlayerDeckNamesKey(player));
        _decks.put(constructDeckKey(player, name), deck);
    }

    @Override
    public final void saveDeckForPlayer(CardDeck deck, User player) {
        String deckName = deck.getDeckName();
        _delegate.saveDeckForPlayer(deck, player);
        _playerDeckNames.remove(constructPlayerDeckNamesKey(player));
        _decks.put(constructDeckKey(player, deckName), deck);
    }

}