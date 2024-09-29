package com.gempukku.stccg.actions.choose;


import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;

public abstract class ChoosePlayerEffect extends UnrespondableEffect {
    protected final String _playerId;

    public ChoosePlayerEffect(ActionContext actionContext) {
        super(actionContext);
        _playerId = actionContext.getPerformingPlayerId();
    }

    @Override
    public void doPlayEffect() {
        String[] players = _game.getAllPlayerIds();
        if (players.length == 1)
            playerChosen(players[0]);
        else
            _game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new MultipleChoiceAwaitingDecision("Choose a player", players) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            playerChosen(result);
                        }
                    });
    }

    protected abstract void playerChosen(String playerId);
}
