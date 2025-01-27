package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.playcard.DownloadCardAction;
import com.gempukku.stccg.actions.playcard.ReportCardAction;
import com.gempukku.stccg.actions.usage.UseGameTextAction;
import com.gempukku.stccg.actions.usage.UseOncePerTurnAction;
import com.gempukku.stccg.cards.blueprints.actionsource.SeedCardActionSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalReportableCard1E;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.GameState;

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
                .getNumberOfCopiesSeededByPlayer(actionContext.getPerformingPlayer(), actionContext.getGame()) < 1);
        actionSource.setSeedZone(Zone.TABLE);
        return actionSource;
    }

    private Collection<PhysicalCard> getDestinationOptionsForCard(DefaultGame cardGame, PhysicalCard card) {
        return Filters.filterYourActive(cardGame, card.getOwner(),
                Filters.yourMatchingOutposts(card.getOwner(), card));
    }

    @Override
    public List<TopLevelSelectableAction> getGameTextActionsWhileInPlay(Player player, PhysicalCard thisCard,
                                                                        DefaultGame cardGame) {
        Phase currentPhase = cardGame.getCurrentPhase();
        List<TopLevelSelectableAction> actions = new LinkedList<>();

        if (currentPhase == Phase.CARD_PLAY) {

            UseGameTextAction action1 = new UseGameTextAction(thisCard, player, "Report a card for free");
                // TODO - This should not be where the Filters.playable filter is included
                // TODO - Make sure there's a native quadrant requirement here if Modern rules are used
            Filterable playableCardFilter = Filters.and(CardType.PERSONNEL, Uniqueness.UNIVERSAL, CardIcon.TNG_ICON,
                    Filters.youHaveNoCopiesInPlay(thisCard.getOwner()), Filters.playable,
                    Filters.not(Filters.android), Filters.not(Filters.hologram), Filters.not(CardIcon.AU_ICON));
            action1.setCardActionPrefix("1");
            action1.appendUsage(new UseOncePerTurnAction(action1, thisCard, player));
            action1.appendEffect(
                    new DownloadCardAction(cardGame, Zone.HAND, thisCard.getOwner(), playableCardFilter) {
                        @Override
                        protected Collection<PhysicalCard> getPlayableCards(DefaultGame cardGame, GameState gameState) {
                            Collection<PhysicalCard> playableCards = Filters.filter(
                                    thisCard.getOwner().getCardsInHand(), playableCardFilter);
                            playableCards.removeIf(card -> getDestinationOptionsForCard(cardGame, card).isEmpty());
                            return playableCards;
                        }

                        @Override
                        protected void playCard(final PhysicalCard selectedCard) throws InvalidGameLogicException {

                            Action action = new ReportCardAction((PhysicalReportableCard1E) selectedCard,
                                    true, Filters.filterYourActive(cardGame, thisCard.getOwner(),
                                    Filters.yourMatchingOutposts(thisCard.getOwner(), thisCard)));
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
        return actions;
    }
}