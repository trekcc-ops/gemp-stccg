package com.gempukku.stccg.actions.choose;


import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Objects;

public abstract class ChoosePlayerExceptEffect extends UnrespondableEffect {
    private final String _playerId;
    private final String _excludedPlayerId;
    private final DefaultGame _game;

    public ChoosePlayerExceptEffect(ActionContext actionContext, String excludedPlayerId) {
        _playerId = actionContext.getPerformingPlayerId();
        _game = actionContext.getGame();
        _excludedPlayerId = excludedPlayerId;
    }

    @Override
    public void doPlayEffect() {
        String[] allPlayers = _game.getAllPlayerIds();
        String[] includedPlayers = new String[allPlayers.length - 1];
        int j = 0;
        for (String allPlayer : allPlayers) {
            if (!Objects.equals(allPlayer, _excludedPlayerId)) {
                includedPlayers[j] = allPlayer;
                j++;
            }
        }
        if (includedPlayers.length == 1)
            playerChosen(includedPlayers[0]);
        else
            _game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new MultipleChoiceAwaitingDecision("Choose a player", includedPlayers) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            playerChosen(result);
                        }
                    });
    }

    protected abstract void playerChosen(String playerId);
}
