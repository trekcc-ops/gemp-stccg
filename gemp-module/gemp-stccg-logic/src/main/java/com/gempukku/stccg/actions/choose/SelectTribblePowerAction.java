package com.gempukku.stccg.actions.choose;


import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Arrays;

public class SelectTribblePowerAction extends MakeDecisionAction {

    private final String _memoryId;
    public SelectTribblePowerAction(DefaultGame cardGame, ActionContext actionContext, String memoryId) {
        super(cardGame, actionContext.getPerformingPlayerId(), "Choose a Tribble power", actionContext);
        _memoryId = memoryId;
    }

    @Override
    protected AwaitingDecision getDecision(DefaultGame cardGame) {
        return new MultipleChoiceAwaitingDecision(_actionContext.getPerformingPlayerId(),
                "Choose a Tribble power", Arrays.asList(TribblePower.names()), cardGame) {
            @Override
            protected void validDecisionMade(int index, String result) {
                _actionContext.setValueToMemory(_memoryId, result);
                setAsSuccessful();
            }
        };
    }

}