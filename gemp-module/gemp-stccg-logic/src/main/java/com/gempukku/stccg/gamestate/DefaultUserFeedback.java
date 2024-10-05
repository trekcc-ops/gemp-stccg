package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.common.AwaitingDecision;
import com.gempukku.stccg.common.UserFeedback;
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
    public void sendAwaitingDecision(String playerId, AwaitingDecision awaitingDecision) {
        _awaitingDecisionMap.put(playerId, awaitingDecision);
        _game.getGameState().playerDecisionStarted(playerId, awaitingDecision);
    }

    @Override
    public boolean hasNoPendingDecisions() {
        return _awaitingDecisionMap.isEmpty();
    }

    public Set<String> getUsersPendingDecision() {
        return _awaitingDecisionMap.keySet();
    }
}
