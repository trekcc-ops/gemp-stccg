package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.choose.SelectVisibleCardAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardsAction;
import com.gempukku.stccg.actions.discard.TribblesMultiDiscardActionBroken;
import com.gempukku.stccg.actions.placecard.PlaceCardsOnBottomOfDrawDeckAction;
import com.gempukku.stccg.actions.scorepoints.ScorePointsAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class ActivateLaughterTribblePowerAction extends ActivateTribblePowerAction {
    private final static int BONUS_POINTS = 25000;
    private String _discardingPlayerId;

    public ActivateLaughterTribblePowerAction(TribblesGame cardGame, PhysicalCard performingCard,
                                              ActionContext actionContext) throws PlayerNotFoundException {
        super(cardGame, actionContext, performingCard);
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        // There must be at least two players with cards in their hands
        int playersWithHands = 0;
        for (Player player : cardGame.getPlayers()) {
            if (!player.getCardsInHand().isEmpty())
                playersWithHands++;
        }
        return playersWithHands >= 2;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        Action cost = getNextCost();
        if (cost != null)
            return cost;

        List<String> players = Arrays.asList(cardGame.getAllPlayerIds());

        for (Player player : cardGame.getPlayers()) {
            if (player.getCardsInHand().isEmpty()) {
                players.remove(player.getPlayerId());
            }
        }

        cardGame.sendAwaitingDecision(
                new MultipleChoiceAwaitingDecision(cardGame.getPlayer(_performingPlayerId),
                        "Choose a player to discard a card", players, cardGame) {
                    @Override
                    protected void validDecisionMade(int index, String result)
                            throws DecisionResultInvalidException {
                        try {
                            firstPlayerChosen(players, result, cardGame);
                        } catch(InvalidGameLogicException | PlayerNotFoundException exp) {
                            throw new DecisionResultInvalidException(exp.getMessage());
                        }
                    }
                });

        return getNextAction();
    }

    private void firstPlayerChosen(List<String> allPlayers, String chosenPlayer, DefaultGame game)
            throws InvalidGameLogicException, PlayerNotFoundException {
        _discardingPlayerId = chosenPlayer;
        List<String> newSelectablePlayers = new ArrayList<>(allPlayers);
        newSelectablePlayers.remove(chosenPlayer);
        if (newSelectablePlayers.size() == 1)
            secondPlayerChosen(Iterables.getOnlyElement(newSelectablePlayers), game);
        else {
            game.sendAwaitingDecision(
                    new MultipleChoiceAwaitingDecision(game.getPlayer(_performingPlayerId),
                            "Choose a player to place a card from hand on the bottom of their deck",
                            newSelectablePlayers, game) {
                        @Override
                        protected void validDecisionMade(int index, String result)
                                throws DecisionResultInvalidException {
                            try {
                                secondPlayerChosen(result, game);
                            } catch(InvalidGameLogicException | PlayerNotFoundException exp) {
                                throw new DecisionResultInvalidException(exp.getMessage());
                            }
                        }
                    });
        }
    }

    private void secondPlayerChosen(String secondPlayerChosen, DefaultGame game)
            throws InvalidGameLogicException, PlayerNotFoundException {
        Player discardingPlayer = game.getPlayer(_discardingPlayerId);
        SelectVisibleCardAction discardSelectAction =
                new SelectVisibleCardAction(game, _discardingPlayerId, "Choose a card to discard",
                        Filters.yourHand(discardingPlayer));
        appendEffect(new TribblesMultiDiscardActionBroken(game, _performingCard, discardingPlayer, discardSelectAction));

        Player performingPlayer = game.getPlayer(_performingPlayerId);
        SelectVisibleCardsAction selectAction = new SelectVisibleCardsAction(game, performingPlayer,
                "Choose a card to put beneath draw deck", Filters.yourHand(performingPlayer),
                2, 2);
        appendEffect(new PlaceCardsOnBottomOfDrawDeckAction(game, performingPlayer, selectAction));

        if (!(Objects.equals(_discardingPlayerId, _performingPlayerId) ||
                Objects.equals(secondPlayerChosen, _performingPlayerId))) {
            appendEffect(new ScorePointsAction(game, _performingCard, discardingPlayer, BONUS_POINTS));
        }
    }


}