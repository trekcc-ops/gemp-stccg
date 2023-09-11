package com.gempukku.lotro.effects.choose;

import com.gempukku.lotro.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.lotro.effects.UnrespondableEffect;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.rules.GameUtils;

import java.util.Objects;

public abstract class ChoosePlayerExceptEffect extends UnrespondableEffect {
    private final String _playerId;
    private final String _excludedPlayerId;

    public ChoosePlayerExceptEffect(String playerId, String excludedPlayerId) {
        _playerId = playerId;
        _excludedPlayerId = excludedPlayerId;
    }

    @Override
    public void doPlayEffect(DefaultGame game) {
        String[] allPlayers = GameUtils.getAllPlayers(game);
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
            game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new MultipleChoiceAwaitingDecision(1, "Choose a player", includedPlayers) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            playerChosen(result);
                        }
                    });
    }

    protected abstract void playerChosen(String playerId);
}
