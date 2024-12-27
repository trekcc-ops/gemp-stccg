package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.choose.SelectVisibleCardAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardsAction;
import com.gempukku.stccg.actions.discard.DiscardCardAction;
import com.gempukku.stccg.actions.placecard.PlaceCardsOnBottomOfDrawDeckAction;
import com.gempukku.stccg.actions.scorepoints.ScorePointsAction;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class ActivateLaughterTribblePowerAction extends ActivateTribblePowerAction {
    private final static int BONUS_POINTS = 25000;
    private String _discardingPlayerId;

    public ActivateLaughterTribblePowerAction(TribblesActionContext actionContext, TribblePower power) {
        super(actionContext, power);
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        // There must be at least two players with cards in their hands
        int playersWithHands = 0;
        for (String player : cardGame.getAllPlayerIds()) {
            if (!cardGame.getGameState().getHand(player).isEmpty())
                playersWithHands++;
        }
        return playersWithHands >= 2;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        Action cost = getNextCost();
        if (cost != null)
            return cost;

        List<String> players = Arrays.asList(cardGame.getAllPlayerIds());
        players.removeIf(player -> cardGame.getGameState().getHand(player).isEmpty());
        cardGame.getUserFeedback().sendAwaitingDecision(
                new MultipleChoiceAwaitingDecision(cardGame.getPlayer(_performingPlayerId),
                        "Choose a player to discard a card", players) {
                    @Override
                    protected void validDecisionMade(int index, String result)
                            throws DecisionResultInvalidException {
                        try {
                            firstPlayerChosen(players, result, cardGame);
                        } catch(InvalidGameLogicException exp) {
                            throw new DecisionResultInvalidException(exp.getMessage());
                        }
                    }
                });

        return getNextAction();
    }

    private void firstPlayerChosen(List<String> allPlayers, String chosenPlayer, DefaultGame game)
            throws InvalidGameLogicException {
        _discardingPlayerId = chosenPlayer;
        List<String> newSelectablePlayers = new ArrayList<>(allPlayers);
        newSelectablePlayers.remove(chosenPlayer);
        if (newSelectablePlayers.size() == 1)
            secondPlayerChosen(Iterables.getOnlyElement(newSelectablePlayers), game);
        else {
            game.getUserFeedback().sendAwaitingDecision(
                    new MultipleChoiceAwaitingDecision(game.getPlayer(_performingPlayerId),
                            "Choose a player to place a card from hand on the bottom of their deck",
                            newSelectablePlayers) {
                        @Override
                        protected void validDecisionMade(int index, String result)
                                throws DecisionResultInvalidException {
                            try {
                                secondPlayerChosen(result, game);
                            } catch(InvalidGameLogicException exp) {
                                throw new DecisionResultInvalidException(exp.getMessage());
                            }
                        }
                    });
        }
    }

    private void secondPlayerChosen(String secondPlayerChosen, DefaultGame game) throws InvalidGameLogicException {
        Player discardingPlayer = game.getPlayer(_discardingPlayerId);
        SelectVisibleCardAction discardSelectAction =
                new SelectVisibleCardAction(_performingCard, discardingPlayer, "Choose a card to discard",
                        Filters.yourHand(discardingPlayer));
        appendAction(new DiscardCardAction(_performingCard, discardingPlayer, discardSelectAction));

        Player performingPlayer = game.getPlayer(_performingPlayerId);
        SelectVisibleCardsAction selectAction = new SelectVisibleCardsAction(_performingCard, performingPlayer,
                "Choose a card to put beneath draw deck", Filters.yourHand(performingPlayer),
                2, 2);
        appendAction(new PlaceCardsOnBottomOfDrawDeckAction(performingPlayer, selectAction, _performingCard));

        if (!(Objects.equals(_discardingPlayerId, _performingPlayerId) ||
                Objects.equals(secondPlayerChosen, _performingPlayerId))) {
            appendAction(new ScorePointsAction(game, _performingCard, _performingPlayerId, BONUS_POINTS));
        }
    }


}