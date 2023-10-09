package com.gempukku.stccg.effects.choose;

import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.effects.UnrespondableEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.GameUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class ChoosePlayerWithCardsInDeckEffect extends UnrespondableEffect {
    private final String _playerId;

    public ChoosePlayerWithCardsInDeckEffect(String playerId) {
        _playerId = playerId;
    }

    @Override
    public void doPlayEffect(DefaultGame game) {
        List<String> playersWithCards = new ArrayList<>();
        for (String player : GameUtils.getAllPlayers(game)) {
            if (game.getGameState().getDrawDeck(player).size() > 0)
                playersWithCards.add(player);
        }
        String[] playersWithCardsArr = playersWithCards.toArray(new String[0]);
        if (playersWithCardsArr.length == 1)
            playerChosen(playersWithCardsArr[0]);
        else
            game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new MultipleChoiceAwaitingDecision(1, "Choose a player", playersWithCardsArr) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            playerChosen(result);
                        }
                    });
    }

    protected abstract void playerChosen(String playerId);
}
