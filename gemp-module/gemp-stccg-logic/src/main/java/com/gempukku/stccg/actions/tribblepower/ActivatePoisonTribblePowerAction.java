package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.discard.DiscardCardsFromEndOfCardPileEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.EndOfPile;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collection;
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
                            protected void validDecisionMade(int index, String result) {
                                playerChosen(result, cardGame);
                            }
                        });
            setProgress(Progress.playerSelected);
        }

        return getNextAction();
    }

    private void playerChosen(String chosenPlayer, DefaultGame game) {
        // That opponent must discard the top card
        ActionContext context = new DefaultActionContext(_performingPlayerId, game, _performingCard,
                null, null);
        appendEffect(new DiscardCardsFromEndOfCardPileEffect(Zone.DRAW_DECK, EndOfPile.TOP,
                chosenPlayer, 1, true, context, null) {
            @Override
            protected void cardsDiscardedCallback(Collection<PhysicalCard> cards) {

                // and you immediately score points equal to the number of tribbles on that card
                PhysicalCard card = Iterables.getOnlyElement(cards);
                game.getGameState().addToPlayerScore(_performingPlayerId, card.getBlueprint().getTribbleValue());
            }
        });
    }


}