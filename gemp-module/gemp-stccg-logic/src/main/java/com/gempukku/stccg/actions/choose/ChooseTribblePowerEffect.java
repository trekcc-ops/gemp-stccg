package com.gempukku.stccg.actions.choose;


import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;

public abstract class ChooseTribblePowerEffect extends UnrespondableEffect {
    private final String _playerId;
    public ChooseTribblePowerEffect(ActionContext actionContext) {
        super(actionContext);
        _playerId = actionContext.getPerformingPlayerId();
    }

    @Override
    public void doPlayEffect() {
        String[] powers = TribblePower.names();
        _game.getUserFeedback().sendAwaitingDecision(_playerId,
                new MultipleChoiceAwaitingDecision("Choose a Tribble power", powers) {
                    @Override
                    protected void validDecisionMade(int index, String result) {
                        powerChosen(result);
                    }
                });
    }

    protected abstract void powerChosen(String playerId);
}
