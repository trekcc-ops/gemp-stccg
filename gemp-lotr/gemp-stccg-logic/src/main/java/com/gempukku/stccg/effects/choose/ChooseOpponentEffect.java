package com.gempukku.stccg.effects.choose;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.rules.GameUtils;

public abstract class ChooseOpponentEffect extends ChoosePlayerEffect {

    public ChooseOpponentEffect(ActionContext actionContext) {
        super(actionContext);
    }

    @Override
    public void doPlayEffect() {
        String[] opponents = GameUtils.getOpponents(_game, _playerId);
        if (opponents.length == 1)
            playerChosen(opponents[0]);
        else
            _game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new MultipleChoiceAwaitingDecision(1, "Choose an opponent", opponents) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            playerChosen(result);
                        }
                    });
    }
}
