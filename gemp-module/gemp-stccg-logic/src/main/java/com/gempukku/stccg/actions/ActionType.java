package com.gempukku.stccg.actions;

public enum ActionType {

    // Don't change these names, because they are passed directly to serialized game state
    ACTIVATE_TRIBBLE_POWER,
    ADD_CARDS_TO_PRESEED_STACK,
    ADD_MODIFIER,
    ALL_PLAYERS_DISCARD,
    ATTEMPT_MISSION,
    BATTLE,
    BEAM_CARDS,
    CHANGE_AFFILIATION,
    DISCARD,
    DOCK_SHIP,
    DOWNLOAD_CARD,
    DRAW_CARD,
    ENCOUNTER_SEED_CARD,
    FAIL_DILEMMA,
    FLY_SHIP,
    KILL,
    MAKE_DECISION,
    NULLIFY,
    OVERCOME_DILEMMA,
    PLACE_CARD_ON_MISSION, // General all-purpose term for putting cards places (like on a deck or on another card)
    PLACE_CARDS_BENEATH_DRAW_DECK,
    PLACE_CARD_ON_TOP_OF_DRAW_DECK,
    PLACE_CARD_IN_PLAY_PILE, // needs to be revisited when Tribbles is implemented
    PLAY_CARD,
    REMOVE_CARD_FROM_GAME,
    REMOVE_CARDS_FROM_PRESEED_STACK,
    REVEAL_SEED_CARD,
    SCORE_POINTS,
    SEED_CARD,
    SELECT_ACTION,
    SELECT_AFFILIATION,
    SELECT_AWAY_TEAM,
    SELECT_CARDS,
    SELECT_SKILL,
    SHUFFLE_CARDS_INTO_DRAW_DECK,
    STOP_CARDS,
    SYSTEM_QUEUE, // Automated Gemp actions, like asking players if they want to perform optional responses to other actions
    UNDOCK_SHIP,
    USAGE_LIMIT, // Using a cost like normal card play or a "once per turn" limit
    USE_GAME_TEXT, // Compound game text action that consists of several sub-actions
    VOLUNTEER_FOR_SELECTION,
    WALK_CARDS
}