package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;

import java.util.LinkedList;
import java.util.List;

public class ChooseOpponentEffect extends ChoosePlayerEffect {

    public ChooseOpponentEffect(ActionContext actionContext, String memoryId) {
        super(actionContext, memoryId);
    }

    @Override
    public void doPlayEffect() {
        List<String> opponents = new LinkedList<>(_game.getGameState().getPlayerOrder().getAllPlayers());
        opponents.remove(_playerId);

        if (opponents.size() == 1)
            playerChosen();
        else
            _game.getUserFeedback().sendAwaitingDecision(
                    new MultipleChoiceAwaitingDecision(_game.getPlayer(_playerId), "Choose an opponent", opponents) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            playerChosen();
                            _context.setValueToMemory(_memoryId, result);
                        }
                    });
    }
}