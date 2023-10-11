package com.gempukku.stccg.effects.choose;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.effects.defaulteffect.UnrespondableEffect;
import com.gempukku.stccg.game.DefaultGame;

public abstract class ChooseTribblePowerEffect extends UnrespondableEffect {
    private final String _playerId;
    private final DefaultGame _game;
    public ChooseTribblePowerEffect(ActionContext actionContext) {
        _playerId = actionContext.getPerformingPlayer();
        _game = actionContext.getGame();
    }

    @Override
    public void doPlayEffect() {
        String[] powers = TribblePower.names();
        _game.getUserFeedback().sendAwaitingDecision(_playerId,
                new MultipleChoiceAwaitingDecision(1, "Choose a Tribble power", powers) {
                    @Override
                    protected void validDecisionMade(int index, String result) {
                        powerChosen(result);
                    }
                });
    }

    protected abstract void powerChosen(String playerId);
}
