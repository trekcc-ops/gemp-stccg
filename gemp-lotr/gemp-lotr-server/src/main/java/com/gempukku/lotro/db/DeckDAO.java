package com.gempukku.lotro.db;

import com.gempukku.lotro.cards.CardDeck;
import com.gempukku.lotro.game.User;

import java.util.Map;
import java.util.Set;

public interface DeckDAO {
    CardDeck getDeckForPlayer(User player, String name);

    void saveDeckForPlayer(User player, String name, String target_format, String notes, CardDeck deck);

    void deleteDeckForPlayer(User player, String name);

    CardDeck renameDeck(User player, String oldName, String newName);

    Set<Map.Entry<String, String>> getPlayerDeckNames(User player);

}
