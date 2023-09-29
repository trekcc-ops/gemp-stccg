package com.gempukku.lotro.tournament;

import com.gempukku.lotro.cards.CardBlueprintLibrary;
import com.gempukku.lotro.cards.CardDeck;

import java.util.Map;
import java.util.Set;

public interface TournamentPlayerDAO {
    void addPlayer(String tournamentId, String playerName, CardDeck deck);

    void updatePlayerDeck(String tournamentId, String playerName, CardDeck deck);

    void dropPlayer(String tournamentId, String playerName);

    Map<String, CardDeck> getPlayerDecks(String tournamentId, String format, CardBlueprintLibrary library);

    Set<String> getDroppedPlayers(String tournamentId);

    CardDeck getPlayerDeck(String tournamentId, String playerName, String format, CardBlueprintLibrary library);

    Set<String> getPlayers(String tournamentId);
}
