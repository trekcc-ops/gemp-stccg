package com.gempukku.stccg.effects.choose;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.effects.defaulteffect.UnrespondableEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.GameUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class ChoosePlayerWithCardsInDeckEffect extends UnrespondableEffect {
    private final String _playerId;
    private final DefaultGame _game;

    public ChoosePlayerWithCardsInDeckEffect(ActionContext actionContext) {
        _game = actionContext.getGame();
        _playerId = actionContext.getPerformingPlayer();
    }

    @Override
    public void doPlayEffect() {
        List<String> playersWithCards = new ArrayList<>();
        for (String player : GameUtils.getAllPlayers(_game)) {
            if (!_game.getGameState().getDrawDeck(player).isEmpty())
                playersWithCards.add(player);
        }
        String[] playersWithCardsArr = playersWithCards.toArray(new String[0]);
        if (playersWithCardsArr.length == 1)
            playerChosen(playersWithCardsArr[0]);
        else
            _game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new MultipleChoiceAwaitingDecision(1, "Choose a player", playersWithCardsArr) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            playerChosen(result);
                        }
                    });
    }

    protected abstract void playerChosen(String playerId);
}
