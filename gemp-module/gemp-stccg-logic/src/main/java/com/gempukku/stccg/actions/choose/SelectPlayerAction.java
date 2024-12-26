package com.gempukku.stccg.actions.choose;


import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;

import java.util.List;

public class SelectPlayerAction extends MakeDecisionAction {

    public SelectPlayerAction(ActionContext actionContext, String memoryId, List<String> playerIds) {
        super(actionContext.getSource(), new MultipleChoiceAwaitingDecision(actionContext.getPerformingPlayer(),
                "Choose a player", playerIds) {
            @Override
            protected void validDecisionMade(int index, String result) {
                actionContext.setValueToMemory(memoryId, result);
            }
        });
    }
}