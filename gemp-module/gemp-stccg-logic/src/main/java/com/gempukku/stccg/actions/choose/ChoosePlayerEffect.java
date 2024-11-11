package com.gempukku.stccg.actions.choose;


import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;

import java.util.Arrays;
import java.util.List;

public class ChoosePlayerEffect extends UnrespondableEffect {
    protected final String _playerId;
    protected final ActionContext _context;
    protected final String _memoryId;

    public ChoosePlayerEffect(ActionContext actionContext, String memoryId) {
        super(actionContext);
        _context = actionContext;
        _playerId = actionContext.getPerformingPlayerId();
        _memoryId = memoryId;
    }


    @Override
    public void doPlayEffect() {
        List<String> players = Arrays.stream(_game.getAllPlayerIds()).toList();
        if (players.size() == 1)
            playerChosen();
        else
            _game.getUserFeedback().sendAwaitingDecision(
                    new MultipleChoiceAwaitingDecision(_game.getPlayer(_playerId), "Choose a player", players) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            _context.setValueToMemory(_memoryId, result);
                            playerChosen();
                        }
                    });
    }

    protected void playerChosen() { }
}