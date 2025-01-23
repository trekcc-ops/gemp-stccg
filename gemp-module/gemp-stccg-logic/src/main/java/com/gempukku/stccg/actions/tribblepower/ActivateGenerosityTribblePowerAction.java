package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.draw.DrawCardsAction;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.PlayerNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class ActivateGenerosityTribblePowerAction extends ActivateTribblePowerAction {
    private final static int BONUS_POINTS = 25000;

    public ActivateGenerosityTribblePowerAction(TribblesActionContext actionContext, TribblePower power) throws PlayerNotFoundException {
        super(actionContext, power);
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        Action cost = getNextCost();
        if (cost != null)
            return cost;

        // You and one other player (your choice) each score 25,000 points.
        List<String> opponents = new ArrayList<>();
        for (String player : cardGame.getAllPlayerIds()) {
            if (!Objects.equals(player, _performingPlayerId))
                opponents.add(player);
        }
        String[] opponentsArray = opponents.toArray(new String[0]);
        if (opponentsArray.length == 1)
            playerChosen(opponentsArray[0], cardGame);
        else
            cardGame.getUserFeedback().sendAwaitingDecision(
                    new MultipleChoiceAwaitingDecision(cardGame.getPlayer(_performingPlayerId),
                            "Choose a player to score 25,000 points", opponentsArray, cardGame) {
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

    private void playerChosen(String chosenPlayer, DefaultGame cardGame) throws PlayerNotFoundException {
        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
        Player chosenPlayerObj = cardGame.getPlayer(chosenPlayer);
        // You and one other player (your choice) each score 25,000 points.
        cardGame.addToPlayerScore(performingPlayer, BONUS_POINTS);
        cardGame.addToPlayerScore(chosenPlayerObj, BONUS_POINTS);

        // Draw a card.
        appendEffect(new DrawCardsAction(_performingCard, cardGame.getPlayer(_performingPlayerId)));
    }


}