package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.PlayOutDecisionEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.IntegerAwaitingDecision;

public class ChooseNumberEffect extends PlayOutDecisionEffect {

    public ChooseNumberEffect(ActionContext context, String choiceText, ValueSource valueSource, String memoryId) {
        super(context.getGame(), new IntegerAwaitingDecision(context, choiceText, valueSource) {
            @Override
            public void decisionMade(String result) throws DecisionResultInvalidException {
                context.setValueToMemory(memoryId, String.valueOf(getValidatedResult(result)));
            }
        });
    }

}