package com.gempukku.stccg.actions.choose;


import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;

import java.util.Arrays;

public class SelectTribblePowerAction extends MakeDecisionAction {
    public SelectTribblePowerAction(ActionContext actionContext, String memoryId) {
        super(actionContext.getSource(),
                new MultipleChoiceAwaitingDecision(actionContext.getPerformingPlayer(),
                        "Choose a Tribble power", Arrays.asList(TribblePower.names()), actionContext.getGame()) {
            @Override
            protected void validDecisionMade(int index, String result) {
                actionContext.setValueToMemory(memoryId, result);
            }
        });
    }

}