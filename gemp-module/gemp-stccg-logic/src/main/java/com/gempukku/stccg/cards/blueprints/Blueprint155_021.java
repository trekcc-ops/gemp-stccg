package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActivateCardAction;
import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.actions.playcard.ChooseAndPlayCardFromZoneEffect;
import com.gempukku.stccg.actions.playcard.ReportCardAction;
import com.gempukku.stccg.actions.turn.OnceEachTurnEffect;
import com.gempukku.stccg.cards.blueprints.actionsource.SeedCardActionSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalReportableCard1E;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Blueprint155_021 extends CardBlueprint {
    Blueprint155_021() {
        super("155_021"); // Attention All Hands
    }

    @Override
    public SeedCardActionSource getSeedCardActionSource() {
        SeedCardActionSource actionSource = new SeedCardActionSource();
        actionSource.addRequirement((actionContext) -> actionContext.getSource()
                .getNumberOfCopiesSeededByPlayer(actionContext.getPerformingPlayer()) < 1);
        actionSource.setSeedZone(Zone.TABLE);
        return actionSource;
    }

    private Collection<PhysicalCard> getDestinationOptionsForCard(PhysicalCard card) {
        return Filters.filterYourActive(card.getOwner(), Filters.yourMatchingOutposts(card.getOwner(), card));
    }

    public List<? extends ActivateCardAction> getInPlayActionsNew(Player player, PhysicalCard card) {
        DefaultGame game = player.getGame();
        Phase currentPhase = game.getCurrentPhase();
        List<ActivateCardAction> actions = new LinkedList<>();

        if (currentPhase == Phase.CARD_PLAY) {

            ActivateCardAction action1 = new ActivateCardAction(card);
                // TODO - This should not be where the Filters.playable filter is included
                // TODO - Make sure there's a native quadrant requirement here if Modern rules are used
            Filterable playableCardFilter = Filters.and(CardType.PERSONNEL, Uniqueness.UNIVERSAL, CardIcon.TNG_ICON,
                    Filters.youHaveNoCopiesInPlay(card.getOwner()), Filters.playable,
                    Filters.not(Filters.android), Filters.not(Filters.hologram), Filters.not(CardIcon.AU_ICON));
            action1.setCardActionPrefix("1");
            action1.appendUsage(new OnceEachTurnEffect(game, action1));
            action1.appendEffect(
                    new ChooseAndPlayCardFromZoneEffect(Zone.HAND, card.getOwner(), playableCardFilter) {
                        @Override
                        protected Collection<PhysicalCard> getPlayableCards() {
                            Collection<PhysicalCard> playableCards = Filters.filter(
                                    game.getGameState().getHand(card.getOwnerName()), playableCardFilter);
                            playableCards.removeIf(card -> getDestinationOptionsForCard(card).isEmpty());
                            return playableCards;
                        }

                        @Override
                        protected void playCard(final PhysicalCard selectedCard) {

                            Action action = new ReportCardAction((PhysicalReportableCard1E) selectedCard,
                                    true, Filters.filterYourActive(card.getOwner(),
                                    Filters.yourMatchingOutposts(card.getOwner(), card)));
                            setPlayCardAction(action);
                            getPlayCardAction().appendEffect(
                                    new UnrespondableEffect(card.getGame()) {
                                        @Override
                                        protected void doPlayEffect() {
                                            afterCardPlayed(selectedCard);
                                        }
                                    });
                            selectedCard.getGame().getActionsEnvironment().addActionToStack(getPlayCardAction());
                        }
                    });
            action1.setText("Report a personnel for free");
            if (action1.canBeInitiated(game))
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