package com.gempukku.stccg.tournament;

import com.gempukku.stccg.cards.CardDeck;

public interface TournamentCallback {
    void createGame(String playerOne, CardDeck deckOne, String playerTwo, CardDeck deckTwo);

    void broadcastMessage(String message);
}
