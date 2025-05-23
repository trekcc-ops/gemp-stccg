package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.playcard.DownloadCardAction;
import com.gempukku.stccg.actions.playcard.ReportCardAction;
import com.gempukku.stccg.actions.usage.UseGameTextAction;
import com.gempukku.stccg.actions.usage.UseOncePerTurnAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalReportableCard1E;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Blueprint155_021 extends CardBlueprint {

    // Attention All Hands

    @Override
    public List<TopLevelSelectableAction> getGameTextActionsWhileInPlay(Player player, PhysicalCard thisCard,
                                                                        DefaultGame cardGame) {
        Phase currentPhase = cardGame.getCurrentPhase();
        List<TopLevelSelectableAction> actions = new LinkedList<>();

        if (currentPhase == Phase.CARD_PLAY) {

            // TODO - This should not be where the Filters.playable filter is included
            // TODO - Make sure there's a native quadrant requirement here if Modern rules are used
            Filterable playableCardFilter = Filters.and(CardType.PERSONNEL, Uniqueness.UNIVERSAL, CardIcon.TNG_ICON,
                    Filters.youHaveNoCopiesInPlay(thisCard.getOwner()), Filters.playable,
                    Filters.not(Filters.android), Filters.not(Filters.hologram), Filters.not(CardIcon.AU_ICON),
                    Filters.inYourHand(player), Filters.youControlAMatchingOutpost(player));

            Collection<PhysicalCard> playableCards = Filters.filter(cardGame, playableCardFilter);
            if (!playableCards.isEmpty()) {

                UseGameTextAction action1 = new UseGameTextAction(thisCard, player, "Report a card for free");
                action1.setCardActionPrefix("1");
                action1.appendUsage(new UseOncePerTurnAction(action1, thisCard, player));
                action1.appendEffect(

                        new DownloadCardAction(cardGame, Zone.HAND, thisCard.getOwner(), playableCardFilter, thisCard) {

                            @Override
                            protected void playCard(final PhysicalCard selectedCard) throws InvalidGameLogicException {

                                CardFilter outpostFilter = Filters.yourMatchingOutposts(thisCard.getOwner(), selectedCard);
                                Collection<PhysicalCard> eligibleDestinations = Filters.filter(cardGame, outpostFilter);

                                Action action = new ReportCardAction((PhysicalReportableCard1E) selectedCard,
                                        true, eligibleDestinations);
                                setPlayCardAction(action);
                                selectedCard.getGame().getActionsEnvironment().addActionToStack(getPlayCardAction());
                            }
                        });
                action1.setText("Report a personnel for free");
                if (action1.canBeInitiated(cardGame))
                    actions.add(action1);

/*            ActivateCardAction action2 = new ActivateCardAction(card);
            Filterable downloadableCardFilter = Filters.and(CardType.SHIP, Uniqueness.UNIVERSAL, Icon1E.TNG_ICON,
                    Filters.playable);
            action2.setCardActionPrefix("2");
            action2.appendUsage(new OncePerGameEffect(action2));
            // append normal card play cost
            action2.appendAction(new DownloadEffect(download a universal [TNG] ship to your matching outpost));
            actions.add(action2);*/
            }
        }
        return actions;
    }
}