package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.blueprints.ActionBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.modifiers.DefaultLimitCounter;
import com.gempukku.stccg.modifiers.LimitCounter;

import java.util.HashMap;
import java.util.Map;

public class ActionLimitCollection {
    private final static int NORMAL_CARD_PLAYS_PER_TURN = 1;

    @JsonProperty("perGame")
    private final Map<String, Map<String, DefaultLimitCounter>> _gameLimitCounters = new HashMap<>();

    @JsonProperty("perTurn")
    private final Map<String, Map<String, DefaultLimitCounter>> _turnLimitActionSourceCounters = new HashMap<>();

    @JsonProperty("normalCardPlays")
    private final Map<String, Integer> _normalCardPlaysAvailable = new HashMap<>();

    public LimitCounter getUntilEndOfGameLimitCounter(String playerName, PhysicalCard card, 
                                                      ActionBlueprint actionBlueprint) {
        CardBlueprint cardBlueprint = card.getBlueprint();
        int actionBlueprintId = cardBlueprint.getIdForActionBlueprint(actionBlueprint);
        String fullActionBlueprintId = card.getBlueprintId() + "_" + actionBlueprintId;
        _gameLimitCounters.computeIfAbsent(fullActionBlueprintId, k -> new HashMap<>());
        return _gameLimitCounters.get(fullActionBlueprintId).computeIfAbsent(playerName, k -> new DefaultLimitCounter());
    }

    public LimitCounter getUntilEndOfTurnLimitCounter(String playerName, PhysicalCard card,
                                                      ActionBlueprint actionBlueprint) {
        CardBlueprint cardBlueprint = card.getBlueprint();
        int actionBlueprintId = cardBlueprint.getIdForActionBlueprint(actionBlueprint);
        String fullActionBlueprintId = card.getBlueprintId() + "_" + actionBlueprintId;
        _turnLimitActionSourceCounters.computeIfAbsent(fullActionBlueprintId, k -> new HashMap<>());
        return _turnLimitActionSourceCounters.get(fullActionBlueprintId).computeIfAbsent(playerName, k -> new DefaultLimitCounter());
    }


    public void signalEndOfTurn() {
        _turnLimitActionSourceCounters.clear();
    }

    public void signalStartOfTurn(String playerName) {
        _normalCardPlaysAvailable.put(playerName, NORMAL_CARD_PLAYS_PER_TURN);
    }

    public int getNormalCardPlaysAvailable(String playerName) {
        return _normalCardPlaysAvailable.get(playerName);
    }

    public void useNormalCardPlay(String playerName) {
        int currentPlaysAvailable = _normalCardPlaysAvailable.get(playerName);
        _normalCardPlaysAvailable.put(playerName, currentPlaysAvailable - 1);
    }

}