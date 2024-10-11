package com.gempukku.stccg.db;

import com.gempukku.stccg.cache.Cached;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.log.LoggingProxy;
import org.apache.commons.collections4.map.LRUMap;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CachedDeckDAO implements DeckDAO, Cached {
    private final DeckDAO _delegate;
    private final Map<String, Set<Map.Entry<String, String>>> _playerDeckNames = Collections.synchronizedMap(new LRUMap<>(100));
    private final Map<String, CardDeck> _decks = Collections.synchronizedMap(new LRUMap<>(100));

    public CachedDeckDAO(DbAccess dbAccess) {
        _delegate = LoggingProxy.createLoggingProxy(DeckDAO.class, new DbDeckDAO(dbAccess));
    }

    @Override
    public void clearCache() {
        _playerDeckNames.clear();
        _decks.clear();
    }

    @Override
    public int getItemCount() {
        return _playerDeckNames.size()+_decks.size();
    }

    private String constructPlayerDeckNamesKey(User player) {
        return player.getName();
    }
    private String constructDeckKey(User player, String name) {
        return player.getName()+"-"+name;
    }

    @Override
    public void deleteDeckForPlayer(User player, String name) {
        _delegate.deleteDeckForPlayer(player, name);
        _playerDeckNames.remove(constructPlayerDeckNamesKey(player));
    }

    @Override
    public CardDeck getDeckForPlayer(User player, String name) {
        String key = constructDeckKey(player, name);
        CardDeck deck = _decks.get(key);
        if (deck == null) {
            deck = _delegate.getDeckForPlayer(player, name);
            _decks.put(key, deck);
        }
        return deck;
    }

    @Override
    public Set<Map.Entry<String, String>> getPlayerDeckNames(User player) {
        String cacheKey = constructPlayerDeckNamesKey(player);
        Set<Map.Entry<String, String>> deckNames = _playerDeckNames.get(cacheKey);
        if (deckNames == null) {
            deckNames = Collections.synchronizedSet(new HashSet<>(_delegate.getPlayerDeckNames(player)));
            _playerDeckNames.put(cacheKey, deckNames);
        }
        return deckNames;
    }

    @Override
    public CardDeck renameDeck(User player, String oldName, String newName) {
        CardDeck deck = _delegate.renameDeck(player, oldName, newName);
        _playerDeckNames.remove(constructPlayerDeckNamesKey(player));
        _decks.remove(constructDeckKey(player, oldName));
        _decks.put(constructDeckKey(player, newName), deck);

        return deck;
    }

    @Override
    public void saveDeckForPlayer(User player, String name, String target_format, String notes, CardDeck deck) {
        _delegate.saveDeckForPlayer(player, name, target_format, notes, deck);
        _playerDeckNames.remove(constructPlayerDeckNamesKey(player));
        _decks.put(constructDeckKey(player, name), deck);
    }
}