package com.gempukku.stccg.actions;

import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;

public class PlayOutDecisionEffect implements Effect {
    private final AwaitingDecision _decision;
    private final DefaultGame _game;

    public PlayOutDecisionEffect(DefaultGame game, AwaitingDecision decision) {
        _game = game;
        _decision = decision;
    }


    @Override
    public String getText() {
        return null;
    }

    @Override
    public void playEffect() {
        getGame().getUserFeedback().sendAwaitingDecision(_decision);
    }

    @Override
    public boolean isPlayableInFull() {
        return true;
    }

    @Override
    public boolean wasCarriedOut() {
        return true;
    }

    public String getPerformingPlayerId() { return _decision.getDecidingPlayer(_game).getPlayerId(); }
    public DefaultGame getGame() { return _game; }

}