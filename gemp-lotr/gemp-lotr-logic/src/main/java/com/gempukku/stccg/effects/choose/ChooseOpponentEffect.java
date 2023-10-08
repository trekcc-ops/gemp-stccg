package com.gempukku.stccg.effects.choose;

import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.GameUtils;
import com.gempukku.stccg.effects.UnrespondableEffect;

public abstract class ChooseOpponentEffect extends UnrespondableEffect {
    private final String _playerId;

    public ChooseOpponentEffect(String playerId) {
        _playerId = playerId;
    }

    @Override
    public void doPlayEffect(DefaultGame game) {
        String[] opponents = GameUtils.getOpponents(game, _playerId);
        if (opponents.length == 1)
            opponentChosen(opponents[0]);
        else
            game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new MultipleChoiceAwaitingDecision(1, "Choose an opponent", opponents) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            opponentChosen(result);
                        }
                    });
    }

    protected abstract void opponentChosen(String opponentId);
}
