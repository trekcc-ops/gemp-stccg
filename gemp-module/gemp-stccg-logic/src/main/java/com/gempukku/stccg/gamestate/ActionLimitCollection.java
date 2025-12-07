package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.actions.blueprints.ActionBlueprint;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.modifiers.DefaultLimitCounter;
import com.gempukku.stccg.modifiers.LimitCounter;

import java.util.HashMap;
import java.util.Map;

public class ActionLimitCollection {
    private final static int NORMAL_CARD_PLAYS_PER_TURN = 1;
    private final Map<String, LimitCounter> _gameLimitCounters = new HashMap<>();
    private final Map<ActionBlueprint, LimitCounter> _turnLimitActionSourceCounters = new HashMap<>();
    private final Map<String, Integer> _normalCardPlaysAvailable = new HashMap<>();

    public LimitCounter getUntilEndOfGameLimitCounter(PhysicalCard card, String prefix) {
        return _gameLimitCounters.computeIfAbsent(prefix + "_" + card.getCardId(), entry -> new DefaultLimitCounter());
    }

    public LimitCounter getUntilEndOfTurnLimitCounter(ActionBlueprint actionBlueprint) {
        return _turnLimitActionSourceCounters.computeIfAbsent(actionBlueprint, entry -> new DefaultLimitCounter());
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