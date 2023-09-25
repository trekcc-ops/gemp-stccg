package com.gempukku.lotro.effects.choose;

import com.gempukku.lotro.common.TribblePower;
import com.gempukku.lotro.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.lotro.effects.UnrespondableEffect;
import com.gempukku.lotro.game.DefaultGame;

public abstract class ChooseTribblePowerEffect extends UnrespondableEffect {
    private final String _playerId;
    public ChooseTribblePowerEffect(String playerId) {
        _playerId = playerId;
    }

    @Override
    public void doPlayEffect(DefaultGame game) {
        String[] powers = TribblePower.names();
        game.getUserFeedback().sendAwaitingDecision(_playerId,
                new MultipleChoiceAwaitingDecision(1, "Choose a Tribble power", powers) {
                    @Override
                    protected void validDecisionMade(int index, String result) {
                        powerChosen(result);
                    }
                });
    }

    protected abstract void powerChosen(String playerId);
}
