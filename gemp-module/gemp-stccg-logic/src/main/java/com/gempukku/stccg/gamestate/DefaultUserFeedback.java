package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.UserFeedback;
import com.gempukku.stccg.game.DefaultGame;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DefaultUserFeedback implements UserFeedback {
    private final Map<String, AwaitingDecision> _awaitingDecisionMap = new HashMap<>();

    private final DefaultGame _game;

    public DefaultUserFeedback(DefaultGame game) {
        _game = game;
    }

    public void removeDecision(String playerId) { _awaitingDecisionMap.remove(playerId); }

    public AwaitingDecision getAwaitingDecision(String playerId) {
        return _awaitingDecisionMap.get(playerId);
    }

    @Override
    public void sendAwaitingDecision(AwaitingDecision awaitingDecision) {
        String decidingPlayerId = awaitingDecision.getDecidingPlayer().getPlayerId();
        _awaitingDecisionMap.put(decidingPlayerId, awaitingDecision);
        _game.getGameState().playerDecisionStarted(decidingPlayerId, awaitingDecision);
    }

    @Override
    public boolean hasNoPendingDecisions() {
        return _awaitingDecisionMap.isEmpty();
    }

    public Set<String> getUsersPendingDecision() {
        return _awaitingDecisionMap.keySet();
    }
}