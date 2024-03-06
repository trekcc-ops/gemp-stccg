package com.gempukku.stccg.cards;

import com.gempukku.stccg.actions.ActivateCardAction;
import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.actions.playcard.ChooseAndPlayCardFromZoneEffect;
import com.gempukku.stccg.actions.playcard.ReportCardAction;
import com.gempukku.stccg.actions.turn.OnceEachTurnEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalReportableCard1E;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.filters.Filters;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Blueprint155_021 extends CardBlueprint {
    Blueprint155_021() {
        super("155_021");
        setTitle("Attention All Hands");
        setCardType(CardType.INCIDENT);
        setPropertyLogo(PropertyLogo.TNG_LOGO);
        setImageUrl("https://www.trekcc.org/1e/cardimages/errata/Attention-All-Hands.jpg");
        addIcons(Icon1E.WARP_CORE);
    }

    private Collection<PhysicalCard> getDestinationOptionsForCard(PhysicalCard card) {
        return Filters.filterYourActive(card.getOwner(), Filters.yourMatchingOutposts(card.getOwner(), card));
    }

    public List<? extends ActivateCardAction> getInPlayActionsNew(Phase phase, PhysicalCard card) {
        List<ActivateCardAction> actions = new LinkedList<>();

        if (phase == Phase.CARD_PLAY) {

            ActivateCardAction action1 = new ActivateCardAction(card);
            Filterable playableCardFilter = Filters.and(CardType.PERSONNEL, Uniqueness.UNIVERSAL, Icon1E.TNG_ICON,
                    Filters.youHaveNoCopiesInPlay(card.getOwner()), Filters.playable,
                    Filters.not(Filters.android), Filters.not(Filters.hologram), Filters.not(Icon1E.AU_ICON));
            action1.setCardActionPrefix("1");
            action1.appendUsage(new OnceEachTurnEffect(action1));
            action1.appendEffect(
                    new ChooseAndPlayCardFromZoneEffect(Zone.HAND, card.getOwner(), playableCardFilter) {
                        @Override
                        protected Collection<PhysicalCard> getPlayableCards() {
                            Collection<PhysicalCard> playableCards = Filters.filter(
                                    card.getGame().getGameState().getHand(card.getOwnerName()), playableCardFilter);
                            playableCards.removeIf(card -> getDestinationOptionsForCard(card).isEmpty());
                            return playableCards;
                        }

                        @Override
                        protected void playCard(final PhysicalCard selectedCard) {
                            setPlayCardAction(new ReportCardAction((PhysicalReportableCard1E) selectedCard, true) {
                                @Override
                                protected Collection<PhysicalCard> getDestinationOptions() {
                                    return getDestinationOptionsForCard(selectedCard);
                                }
                                              });
                            getPlayCardAction().appendEffect(
                                    new UnrespondableEffect() {
                                        @Override
                                        protected void doPlayEffect() {
                                            afterCardPlayed(selectedCard);
                                        }
                                    });
                            selectedCard.getGame().getActionsEnvironment().addActionToStack(getPlayCardAction());
                        }
                    });
            action1.setText("Report a personnel for free");
            if (action1.canBeInitiated())
                actions.add(action1);

/*            ActivateCardAction action2 = new ActivateCardAction(card);
            Filterable downloadableCardFilter = Filters.and(CardType.SHIP, Uniqueness.UNIVERSAL, Icon1E.TNG_ICON,
                    Filters.playable);
            action2.setCardActionPrefix("2");
            action2.appendUsage(new OncePerGameEffect(action2));
            action2.appendCost(new NormalCardPlayCost());
            action2.appendEffect(new DownloadEffect(download a universal [TNG] ship to your matching outpost));
            actions.add(action2);*/
        }
        return actions;
    }
}