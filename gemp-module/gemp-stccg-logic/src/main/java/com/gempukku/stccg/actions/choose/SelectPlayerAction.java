package com.gempukku.stccg.actions.choose;


import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;

import java.util.List;

public class SelectPlayerAction extends MakeDecisionAction {

    private final List<String> _selectablePlayerIds;
    private final String _memoryId;

    public SelectPlayerAction(DefaultGame cardGame, GameTextContext actionContext, String memoryId,
                              List<String> playerIds) {
        super(cardGame, actionContext.yourName(), "Choose a player", actionContext);
        _selectablePlayerIds = playerIds;
        _memoryId = memoryId;
    }


    @Override
    protected AwaitingDecision getDecision(DefaultGame cardGame) {
        return new MultipleChoiceAwaitingDecision(_actionContext.yourName(), "Choose a player",
                _selectablePlayerIds, cardGame) {

            @Override
            protected void validDecisionMade(int index, String result) {
                _actionContext.setValueToMemory(_memoryId, result);
                setAsSuccessful();
            }
        };
    }

}