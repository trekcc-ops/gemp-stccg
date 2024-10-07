package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;

import java.util.LinkedList;
import java.util.List;

public class ChooseOpponentEffect extends ChoosePlayerEffect {

    public ChooseOpponentEffect(ActionContext actionContext, String memoryId) {
        super(actionContext, memoryId);
    }

    public static String[] getOpponents(DefaultGame game, String playerId) {
        List<String> shadowPlayers = new LinkedList<>(game.getGameState().getPlayerOrder().getAllPlayers());
        shadowPlayers.remove(playerId);
        return shadowPlayers.toArray(new String[0]);
    }

    @Override
    public void doPlayEffect() {
        String[] opponents = getOpponents(_game, _playerId);
        if (opponents.length == 1)
            playerChosen();
        else
            _game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new MultipleChoiceAwaitingDecision("Choose an opponent", opponents) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            playerChosen();
                            _context.setValueToMemory(_memoryId, result);
                        }
                    });
    }
}