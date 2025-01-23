package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.UserFeedback;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.PlayerNotFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DefaultUserFeedback implements UserFeedback {
    private final Map<String, AwaitingDecision> _awaitingDecisionMap = new HashMap<>();
    private int nextDecisionId = 1;

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
        try {
            String decidingPlayerId = awaitingDecision.getDecidingPlayerId();
            _awaitingDecisionMap.put(decidingPlayerId, awaitingDecision);
            _game.getGameState().playerDecisionStarted(_game, decidingPlayerId, awaitingDecision);
        } catch(PlayerNotFoundException exp) {
            _game.sendErrorMessage(exp);
            _game.cancelGame();
        }
    }

    @Override
    public boolean hasNoPendingDecisions() {
        return _awaitingDecisionMap.isEmpty();
    }

    public Set<String> getUsersPendingDecision() {
        return _awaitingDecisionMap.keySet();
    }

    @Override
    public int getNextDecisionIdAndIncrement() {
        int result = nextDecisionId;
        nextDecisionId++;
        return result;
    }
}