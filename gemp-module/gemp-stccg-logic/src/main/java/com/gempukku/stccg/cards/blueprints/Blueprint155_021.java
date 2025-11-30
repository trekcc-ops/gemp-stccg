package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.ActionCardResolver;
import com.gempukku.stccg.actions.SelectCardsResolver;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.choose.SelectCardAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardAction;
import com.gempukku.stccg.actions.playcard.DownloadReportableAction;
import com.gempukku.stccg.actions.playcard.SelectAndReportForFreeCardAction;
import com.gempukku.stccg.actions.usage.UseNormalCardPlayAction;
import com.gempukku.stccg.actions.usage.UseOncePerGameAction;
import com.gempukku.stccg.actions.usage.UseOncePerTurnAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.filters.MatchingFilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unused")
public class Blueprint155_021 extends CardBlueprint {

    // Attention All Hands

    @Override
    public List<TopLevelSelectableAction> getGameTextActionsWhileInPlay(Player player, PhysicalCard thisCard,
                                                                        DefaultGame cardGame) {
        Phase currentPhase = cardGame.getCurrentPhase();
        List<TopLevelSelectableAction> actions = new LinkedList<>();

        if (currentPhase == Phase.CARD_PLAY && thisCard.isControlledBy(player)) {

            Filterable playableCardFilter = Filters.and(CardType.PERSONNEL, Uniqueness.UNIVERSAL, CardIcon.TNG_ICON,
                    Filters.youHaveNoCopiesInPlay(thisCard.getOwnerName()),
                    Filters.notAny(Filters.android, Filters.hologram, CardIcon.AU_ICON),
                    Filters.inYourHand(player), Filters.youControlAMatchingOutpost(player));

            Collection<PhysicalCard> playableCards = Filters.filter(cardGame, playableCardFilter);
            if (!playableCards.isEmpty()) {

                SelectCardAction selectAction = new SelectVisibleCardAction(cardGame, player,
                        "Select a card to report", playableCards);
                ActionCardResolver cardTarget = new SelectCardsResolver(selectAction);
                MatchingFilterBlueprint destinationFilterBlueprint =
                        new MatchingFilterBlueprint(cardTarget, Filters.your(player), FacilityType.OUTPOST);
                SelectAndReportForFreeCardAction action1 =
                        new SelectAndReportForFreeCardAction(cardGame, thisCard.getOwnerName(), cardTarget, thisCard,
                                destinationFilterBlueprint);
                action1.setCardActionPrefix("1");
                action1.appendUsage(new UseOncePerTurnAction(cardGame, action1, thisCard, player.getPlayerId()));
                if (action1.canBeInitiated(cardGame))
                    actions.add(action1);

            }

            /* TODO - Need to get more explicit somewhere to prevent downloading cards in play, or accepting
                  out-of-play cards as destination targets.
             */
            Filterable downloadableCardFilter = Filters.and(CardType.SHIP, Uniqueness.UNIVERSAL, CardIcon.TNG_ICON);
            Collection<PhysicalCard> downloadableCards = Filters.filter(cardGame, downloadableCardFilter);

            if (!downloadableCards.isEmpty()) {

                SelectCardAction selectAction = new SelectVisibleCardAction(cardGame, player,
                        "Select a card to download", downloadableCards);
                ActionCardResolver cardTarget = new SelectCardsResolver(selectAction);
                MatchingFilterBlueprint destinationFilterBlueprint =
                        new MatchingFilterBlueprint(cardTarget, Filters.your(player), FacilityType.OUTPOST);
                DownloadReportableAction action2 =
                        new DownloadReportableAction(cardGame, player, cardTarget, thisCard, destinationFilterBlueprint);
                action2.setCardActionPrefix("2");
                action2.appendUsage(new UseOncePerGameAction(action2, thisCard, player));
                action2.appendCost(new UseNormalCardPlayAction(cardGame, player));
                if (action2.canBeInitiated(cardGame))
                    actions.add(action2);
            }
        }

        return actions;
    }
}