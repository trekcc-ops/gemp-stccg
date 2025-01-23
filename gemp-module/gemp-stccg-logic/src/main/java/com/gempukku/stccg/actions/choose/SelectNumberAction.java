package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.IntegerAwaitingDecision;
import com.gempukku.stccg.game.PlayerNotFoundException;

public class SelectNumberAction extends MakeDecisionAction {

    public SelectNumberAction(ActionContext context, String choiceText, ValueSource valueSource, String memoryId)
            throws PlayerNotFoundException {
        super(context.getGame(), new IntegerAwaitingDecision(context.getPerformingPlayer(),
                context.substituteText(choiceText), valueSource.getMinimum(context), valueSource.getMaximum(context),
                context.getGame()) {
            @Override
            public void decisionMade(String result) throws DecisionResultInvalidException {
                context.setValueToMemory(memoryId, String.valueOf(getValidatedResult(result)));
            }
        });
    }

}