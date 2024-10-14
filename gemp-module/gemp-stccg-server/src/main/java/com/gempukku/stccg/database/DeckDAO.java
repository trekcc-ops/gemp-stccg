package com.gempukku.stccg.database;

import com.gempukku.stccg.common.CardDeck;

import java.util.Map;
import java.util.Set;

public interface DeckDAO {
    CardDeck getDeckForPlayer(User player, String name);

    void saveDeckForPlayer(User player, String name, String targetFormat, String notes, CardDeck deck);

    void deleteDeckForPlayer(User player, String name);

    CardDeck renameDeck(User player, String oldName, String newName);

    Set<Map.Entry<String, String>> getPlayerDeckNames(User player);

}