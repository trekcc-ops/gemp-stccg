package com.gempukku.stccg.actions.turn;

public enum EndGameActionType {

    // If adding options to this, let the client know!

    ALL_PLAYERS_CANCELLED,
    CONCEDED,
    DECISION_TIMEOUT,
    ERROR,
    LAST_PLAYER_REMAINING,
    PLAYER_TIMEOUT,
    TIE,
    WINNING_SCORE

}