package com.gempukku.stccg.effects;

import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.effects.utils.EffectType;
import com.gempukku.stccg.game.DefaultGame;

public class PlayOutDecisionEffect implements Effect {
    private final String _playerId;
    private final AwaitingDecision _decision;
    private final DefaultGame _game;

    public PlayOutDecisionEffect(DefaultGame game, String playerId, AwaitingDecision decision) {
        _playerId = playerId;
        _decision = decision;
        _game = game;
    }

    @Override
    public String getText() {
        return null;
    }

    @Override
    public EffectType getType() {
        return null;
    }

    @Override
    public void playEffect() {
        _game.getUserFeedback().sendAwaitingDecision(_playerId, _decision);
    }

    @Override
    public boolean isPlayableInFull() {
        return true;
    }

    @Override
    public boolean wasCarriedOut() {
        return true;
    }

}
