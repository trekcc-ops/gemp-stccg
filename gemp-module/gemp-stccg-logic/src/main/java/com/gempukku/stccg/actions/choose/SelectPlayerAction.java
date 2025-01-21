package com.gempukku.stccg.actions.choose;


import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;

import java.util.List;

public class SelectPlayerAction extends MakeDecisionAction {

    public SelectPlayerAction(ActionContext actionContext, String memoryId, List<String> playerIds) {
        super(actionContext.getSource(), new MultipleChoiceAwaitingDecision(actionContext.getPerformingPlayer(),
                "Choose a player", playerIds, actionContext.getGame()) {
            @Override
            protected void validDecisionMade(int index, String result) {
                actionContext.setValueToMemory(memoryId, result);
            }
        });
    }
}