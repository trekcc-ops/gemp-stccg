package com.gempukku.stccg.effects.choose;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.effects.defaulteffect.UnrespondableEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.GameUtils;

public abstract class ChoosePlayerEffect extends UnrespondableEffect {
    protected final String _playerId;
    protected final DefaultGame _game;

    public ChoosePlayerEffect(ActionContext actionContext) {
        _playerId = actionContext.getPerformingPlayer();
        _game = actionContext.getGame();
    }

    @Override
    public void doPlayEffect() {
        String[] players = _game.getAllPlayers();
        if (players.length == 1)
            playerChosen(players[0]);
        else
            _game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new MultipleChoiceAwaitingDecision(1, "Choose a player", players) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            playerChosen(result);
                        }
                    });
    }

    protected abstract void playerChosen(String playerId);
}
