package com.gempukku.stccg.database;

import com.gempukku.stccg.common.AppConfig;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.service.AdminService;

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

    default CardDeck getDeckIfOwnedOrInLibrary(User requestingUser, String deckName, AdminService adminService)
            throws DeckNotFoundException {
        if (getDeckForUser(requestingUser, deckName) instanceof CardDeck userDeck) {
            return userDeck;
        } else {
            try {
                User librarianUser = adminService.getPlayer(AppConfig.getLibrarianUsername());
                if (getDeckForUser(librarianUser, deckName) instanceof CardDeck libraryDeck) {
                    return libraryDeck;
                }
            } catch(UserNotFoundException ignored) {
                // No specific error handling beyond the DeckNotFoundException that will be thrown by this method
            }
        }
        throw new DeckNotFoundException("Could not find deck '" + deckName + "' in your decks or the deck library");
    }
}