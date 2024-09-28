package com.gempukku.stccg.actions.choose;


import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;

import java.util.ArrayList;
import java.util.List;

public abstract class ChoosePlayerWithCardsInDeckEffect extends UnrespondableEffect {
    private final String _playerId;

    public ChoosePlayerWithCardsInDeckEffect(ActionContext actionContext) {
        super(actionContext);
        _playerId = actionContext.getPerformingPlayerId();
    }

    @Override
    public void doPlayEffect() {
        List<String> playersWithCards = new ArrayList<>();
        for (String player : _game.getAllPlayerIds()) {
            if (!_game.getGameState().getDrawDeck(player).isEmpty())
                playersWithCards.add(player);
        }
        String[] playersWithCardsArr = playersWithCards.toArray(new String[0]);
        if (playersWithCardsArr.length == 1)
            playerChosen(playersWithCardsArr[0]);
        else
            _game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new MultipleChoiceAwaitingDecision("Choose a player", playersWithCardsArr) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            playerChosen(result);
                        }
                    });
    }

    protected abstract void playerChosen(String playerId);
}
