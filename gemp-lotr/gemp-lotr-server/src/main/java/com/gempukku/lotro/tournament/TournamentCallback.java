package com.gempukku.lotro.tournament;

import com.gempukku.lotro.cards.CardDeck;

public interface TournamentCallback {
    void createGame(String playerOne, CardDeck deckOne, String playerTwo, CardDeck deckTwo);

    void broadcastMessage(String message);
}
