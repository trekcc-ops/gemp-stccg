package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.discard.DiscardCardAction;
import com.gempukku.stccg.actions.scorepoints.ScorePointsAction;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class ActivatePoisonTribblePowerAction extends ActivateTribblePowerAction {

    private enum Progress { playerSelected }

    public ActivatePoisonTribblePowerAction(TribblesActionContext actionContext, TribblePower power)
            throws InvalidGameLogicException {
        super(actionContext, power, Progress.values());
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        Action cost = getNextCost();
        if (cost != null)
            return cost;

        if (!getProgress(Progress.playerSelected)) {

            // Choose any opponent who still has card(s) in their draw deck.
            List<String> playersWithCards = new ArrayList<>();
            for (String player : cardGame.getAllPlayerIds()) {
                if ((!cardGame.getGameState().getDrawDeck(player).isEmpty()) && !Objects.equals(player, _performingPlayerId))
                    playersWithCards.add(player);
            }
            String[] playersWithCardsArr = playersWithCards.toArray(new String[0]);
            if (playersWithCardsArr.length == 1)
                playerChosen(playersWithCardsArr[0], cardGame);
            else
                cardGame.getUserFeedback().sendAwaitingDecision(
                        new MultipleChoiceAwaitingDecision(cardGame.getPlayer(_performingPlayerId), "Choose a player",
                                playersWithCardsArr) {
                            @Override
                            protected void validDecisionMade(int index, String result) throws InvalidGameLogicException {
                                playerChosen(result, cardGame);
                            }
                        });
            setProgress(Progress.playerSelected);
        }

        return getNextAction();
    }

    private void playerChosen(String chosenPlayerId, DefaultGame game) throws InvalidGameLogicException {
        // That opponent must discard the top card
        Player chosenPlayer = game.getPlayer(chosenPlayerId);
        PhysicalCard discardingCard = game.getGameState().getDrawDeck(chosenPlayerId).getLast();
        DiscardCardAction discardAction = new DiscardCardAction(_performingCard, chosenPlayer, discardingCard);
        appendEffect(discardAction);
        appendEffect(new ScorePointsAction(game, _performingCard, _performingPlayerId,
                discardingCard.getBlueprint().getTribbleValue()));
    }


}