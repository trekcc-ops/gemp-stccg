package com.gempukku.lotro.tournament;

import com.gempukku.lotro.cards.LotroDeck;

import java.util.Map;
import java.util.Set;

public interface TournamentPlayerDAO {
    void addPlayer(String tournamentId, String playerName, LotroDeck deck);

    void updatePlayerDeck(String tournamentId, String playerName, LotroDeck deck);

    void dropPlayer(String tournamentId, String playerName);

    Map<String, LotroDeck> getPlayerDecks(String tournamentId, String format);

    Set<String> getDroppedPlayers(String tournamentId);

    LotroDeck getPlayerDeck(String tournamentId, String playerName, String format);

    Set<String> getPlayers(String tournamentId);
}
