package com.gempukku.stccg.database;

import com.gempukku.stccg.common.CardDeck;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DeckDAO {
    CardDeck getDeckForUser(User user, String name);

    void saveDeckForPlayer(User player, String name, String targetFormat, String notes, CardDeck deck);

    void saveDeckForPlayer(CardDeck deck, User player);

    void deleteDeckForPlayer(User player, String name);

    CardDeck renameDeck(User player, String oldName, String newName) throws DeckNotFoundException;

    Set<Map.Entry<String, String>> getPlayerDeckNames(User player);

    default List<CardDeck> getUserDecks(User user) {
        List<CardDeck> result = new ArrayList<>();
        Set<Map.Entry<String, String>> decks = getPlayerDeckNames(user);
        for (Map.Entry<String, String> deckEntry : decks) {
            String deckName = deckEntry.getValue();
            result.add(getDeckForUser(user, deckName));
        }
        return result;
    }

}