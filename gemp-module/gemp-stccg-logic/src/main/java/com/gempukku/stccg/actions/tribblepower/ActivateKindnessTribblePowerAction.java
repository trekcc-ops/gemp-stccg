package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.choose.ChooseCardsFromZoneEffect;
import com.gempukku.stccg.actions.choose.SelectVisibleCardsAction;
import com.gempukku.stccg.actions.draw.DrawCardAction;
import com.gempukku.stccg.actions.placecard.PlaceCardsOnBottomOfDrawDeckAction;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.TribblesGame;

import java.util.Collection;


public class ActivateKindnessTribblePowerAction extends ActivateTribblePowerAction {

    public ActivateKindnessTribblePowerAction(TribblesActionContext actionContext, TribblePower power) {
        super(actionContext, power);
        TribblesGame cardGame = actionContext.getGame();
        appendAction(new DrawCardAction(_performingCard, cardGame.getPlayer(_performingPlayerId)));
        // TODO: Does this work correctly if you only have 4 cards in hand after the draw?
        for (String player : cardGame.getPlayerIds()) {
            if (cardGame.getGameState().getHand(player).size() >= 4) {
                appendAction(new SubAction(this,
                        new ChooseCardsFromZoneEffect(cardGame, Zone.HAND, player, 1, 1) {
                            @Override
                            protected void cardsSelected(DefaultGame game, Collection<PhysicalCard> selectedCards) {
                                game.getGameState().removeCardsFromZone(player, selectedCards);
                                for (PhysicalCard card : selectedCards) {
                                    game.sendMessage(
                                            player + " puts " + card.getCardLink() + " from hand on bottom of their play pile");
                                    game.getGameState().addCardToZone(card, Zone.PLAY_PILE, false);
                                }
                            }
                        }));
            }
        }
        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
        SelectVisibleCardsAction selectionAction = new SelectVisibleCardsAction(_performingCard, performingPlayer,
                "Choose a card to put beneath draw deck", Filters.yourHand(performingPlayer),
                1, 1);
        appendAction(new PlaceCardsOnBottomOfDrawDeckAction(performingPlayer, selectionAction, _performingCard));
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return (cardGame.getGameState().getHand(_performingPlayerId).size() >= 4);
    }

}