package com.gempukku.stccg.decisions;

public enum DecisionContext {
    RETURN_FIRE("Do you want to return fire?"),
    SEED_MISSION_INDEX_SELECTION("Select location for mission"),
    SELECT_END_OF_TURN_ACTION("Play end of turn action or pass"),
    SELECT_MISSION_FOR_PRESEEDING("Select a mission to seed cards under or remove seed cards from"),
    SELECT_MISSION_PHASE_ACTION("Play mission seed phase action"),
    SELECT_NUMBER("Choose a number"),
    SELECT_OPTIONAL_RESPONSE_ACTION("Optional responses"),
    SELECT_PHASE_ACTION("Play current phase action or pass"),
    SELECT_PLAYER("Choose a player"),
    SELECT_REQUIRED_RESPONSE_ACTION("Required responses"),
    SELECT_TRIBBLES_ACTION(""),
    GENERAL_MULTIPLE_CHOICE("");

    private final String _clientText;

    DecisionContext(String clientText) {
        _clientText = clientText;
    }

    public String getClientText() {
        return _clientText;
    }
}