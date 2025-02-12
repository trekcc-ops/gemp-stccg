package com.gempukku.stccg.actions.choose;


import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Arrays;

public class SelectTribblePowerAction extends MakeDecisionAction {

    private final ActionContext _actionContext;
    private final String _memoryId;
    public SelectTribblePowerAction(ActionContext actionContext, String memoryId) {
        super(actionContext.getGame(), actionContext.getPerformingPlayer(), "Choose a Tribble power");
        _actionContext = actionContext;
        _memoryId = memoryId;
    }

    @Override
    protected AwaitingDecision getDecision(DefaultGame cardGame) {
        return new MultipleChoiceAwaitingDecision(_actionContext.getPerformingPlayer(),
                "Choose a Tribble power", Arrays.asList(TribblePower.names()), cardGame) {
            @Override
            protected void validDecisionMade(int index, String result) {
                _actionContext.setValueToMemory(_memoryId, result);
                setAsSuccessful();
            }
        };
    }

}