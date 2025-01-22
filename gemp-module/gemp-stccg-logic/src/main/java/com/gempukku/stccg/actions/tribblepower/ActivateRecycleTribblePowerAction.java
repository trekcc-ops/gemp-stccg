package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.placecard.ShuffleCardsIntoDrawDeckAction;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.PlayerNotFoundException;

import java.util.ArrayList;
import java.util.List;


public class ActivateRecycleTribblePowerAction extends ActivateTribblePowerAction {

    public ActivateRecycleTribblePowerAction(TribblesActionContext actionContext, TribblePower power) throws PlayerNotFoundException {
        super(actionContext, power);
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws PlayerNotFoundException {
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
                            playersWithCards, cardGame) {
                        @Override
                        protected void validDecisionMade(int index, String result)
                                throws DecisionResultInvalidException {
                            try {
                                playerChosen(result, cardGame);
                            } catch(PlayerNotFoundException exp) {
                                throw new DecisionResultInvalidException(exp.getMessage());
                            }
                        }
                    });
        return getNextAction();
    }

    private void playerChosen(String chosenPlayer, DefaultGame game) throws PlayerNotFoundException {
        Action shuffleAction = new ShuffleCardsIntoDrawDeckAction(
                _performingCard, game.getPlayer(chosenPlayer), Filters.yourDiscard(chosenPlayer));
        appendEffect(shuffleAction);
    }



}