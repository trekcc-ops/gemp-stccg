package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.placecard.ShuffleCardsIntoDrawDeckAction;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.ArrayList;
import java.util.List;


public class ActivateRecycleTribblePowerAction extends ActivateTribblePowerAction {

    public ActivateRecycleTribblePowerAction(TribblesActionContext actionContext, TribblePower power) {
        super(actionContext, power);
    }

    @Override
    public Action nextAction(DefaultGame cardGame) {
        // Choose a player to shuffle his or her discard pile into his or her draw deck
        List<String> playersWithCards = new ArrayList<>();
        for (String player : cardGame.getAllPlayerIds()) {
            if (!cardGame.getGameState().getDiscard(player).isEmpty())
                playersWithCards.add(player);
        }
        if (playersWithCards.size() == 1)
            playerChosen(playersWithCards.getFirst(), cardGame);
        else
            cardGame.getUserFeedback().sendAwaitingDecision(
                    new MultipleChoiceAwaitingDecision(cardGame.getPlayer(_performingPlayerId), "Choose a player",
                            playersWithCards) {
                        @Override
                        protected void validDecisionMade(int index, String result) {
                            playerChosen(result, cardGame);
                        }
                    });
        return getNextAction();
    }

    private void playerChosen(String chosenPlayer, DefaultGame game) {
        Action shuffleAction = new ShuffleCardsIntoDrawDeckAction(
                _performingCard, game.getPlayer(chosenPlayer), Filters.yourDiscard(chosenPlayer));
        appendAction(shuffleAction);
    }



}