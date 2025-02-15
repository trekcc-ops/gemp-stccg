package com.gempukku.stccg.actions;

public enum ActionType {

    // Don't change these names, because they are passed directly to serialized game state
    ACTIVATE_TRIBBLE_POWER,
    ADD_MODIFIER,
    ALL_PLAYERS_DISCARD,
    ATTEMPT_MISSION,
    BATTLE,
    BEAM_CARDS,
    CHANGE_AFFILIATION,
    DISCARD,
    DOWNLOAD_CARD,
    DRAW_CARD,
    ENCOUNTER_SEED_CARD,
    FAIL_DILEMMA,
    KILL,
    MAKE_DECISION,
    MOVE_SHIP,
    OVERCOME_DILEMMA,
    PLACE_CARD, // General all-purpose term for putting cards places (like on a deck or on another card)
    PLAY_CARD,
    REMOVE_CARD_FROM_GAME,
    REVEAL_SEED_CARD,
    SCORE_POINTS,
    SEED_CARD,
    SELECT_ACTION,
    SELECT_AFFILIATION,
    SELECT_AWAY_TEAM,
    SELECT_CARDS,
    SELECT_SKILL,
    STOP_CARDS,
    SYSTEM_QUEUE, // Automated Gemp actions, like asking players if they want to perform optional responses to other actions
    USAGE_LIMIT, // Using a cost like normal card play or a "once per turn" limit
    USE_GAME_TEXT, // Compound game text action that consists of several sub-actions
    WALK_CARDS
}