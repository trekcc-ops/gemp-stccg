package com.gempukku.stccg.game;

import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.database.User;

public class GameParticipant {
    private final String _playerId;
    private final CardDeck _deck;

    public GameParticipant(String playerId, CardDeck deck) {
        _playerId = playerId;
        _deck = deck;
    }

    public GameParticipant(User user, CardDeck deck) {
        _playerId = user.getName();
        _deck = deck;
    }

    public final String getPlayerId() {
        return _playerId;
    }

    public final CardDeck getDeck() {
        return _deck;
    }
}