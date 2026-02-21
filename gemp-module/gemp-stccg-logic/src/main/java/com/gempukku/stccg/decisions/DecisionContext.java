package com.gempukku.stccg.decisions;

public enum DecisionContext {

    // Action selections
    SELECT_MISSION_FOR_SEED_CARDS("Select a mission to seed cards under or remove seed cards from"),
    SELECT_OPTIONAL_RESPONSE_ACTION("Optional responses"),
    SELECT_PHASE_ACTION("Play current phase action or pass"),
    SELECT_REQUIRED_RESPONSE_ACTION("Required responses"),
    SELECT_TRIBBLES_ACTION(""),


    // New stuff
    SHIP_BATTLE_TARGETS("Select targets for ship battle"),


    // Multiple choice
    RETURN_FIRE("Do you want to return fire?"),
    SEED_MISSION_INDEX_SELECTION("Select location for mission"),
    SELECT_PLAYER("Choose a player"),

    // Integer
    SELECT_NUMBER("Choose a number"),
    GENERAL_MULTIPLE_CHOICE(""),
    SELECT_NUMBER_OF_CARDS_TO_DRAW("Select number of cards to draw");

    private final String _clientText;

    DecisionContext(String clientText) {
        _clientText = clientText;
    }

    public String getClientText() {
        return _clientText;
    }
}