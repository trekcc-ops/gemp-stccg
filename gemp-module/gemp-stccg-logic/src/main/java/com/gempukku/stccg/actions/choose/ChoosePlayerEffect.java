package com.gempukku.stccg.actions.choose;


import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;

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
        String[] players = _game.getAllPlayerIds();
        if (players.length == 1)
            playerChosen();
        else
            _game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new MultipleChoiceAwaitingDecision("Choose a player", players) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            _context.setValueToMemory(_memoryId, result);
                            playerChosen();
                        }
                    });
    }

    protected void playerChosen() { }
}