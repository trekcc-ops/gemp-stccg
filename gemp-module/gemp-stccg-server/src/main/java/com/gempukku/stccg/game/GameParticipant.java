package com.gempukku.stccg.game;

import com.gempukku.stccg.cards.CardDeck;

public class GameParticipant {
    private final String _playerId;
    private final CardDeck _deck;

    public GameParticipant(String playerId, CardDeck deck) {
        _playerId = playerId;
        _deck = deck;
    }

    public String getPlayerId() {
        return _playerId;
    }

    public CardDeck getDeck() {
        return _deck;
    }
}
