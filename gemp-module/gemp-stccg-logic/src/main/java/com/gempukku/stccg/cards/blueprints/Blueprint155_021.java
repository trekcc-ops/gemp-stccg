package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionCardResolver;
import com.gempukku.stccg.actions.SelectCardsResolver;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.choose.SelectCardAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardAction;
import com.gempukku.stccg.actions.playcard.ReportCardAction;
import com.gempukku.stccg.actions.playcard.SelectAndReportCardAction;
import com.gempukku.stccg.actions.usage.UseOncePerTurnAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalReportableCard1E;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.filters.MatchingFilterBlueprint;
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

        if (currentPhase == Phase.CARD_PLAY && thisCard.isControlledBy(player)) {

            // TODO - This should not be where the Filters.playable filter is included
            // TODO - Make sure there's a native quadrant requirement here if Modern rules are used
            Filterable playableCardFilter = Filters.and(CardType.PERSONNEL, Uniqueness.UNIVERSAL, CardIcon.TNG_ICON,
                    Filters.youHaveNoCopiesInPlay(thisCard.getOwner()),
                    Filters.not(Filters.android), Filters.not(Filters.hologram), Filters.not(CardIcon.AU_ICON),
                    Filters.inYourHand(player), Filters.youControlAMatchingOutpost(player));

            Collection<PhysicalCard> playableCards = Filters.filter(cardGame, playableCardFilter);
            if (!playableCards.isEmpty()) {

                SelectCardAction selectAction = new SelectVisibleCardAction(cardGame, player,
                        "Select a card to report", playableCards);
                ActionCardResolver cardTarget = new SelectCardsResolver(selectAction);
                MatchingFilterBlueprint destinationFilterBlueprint =
                        new MatchingFilterBlueprint(cardTarget, Filters.your(player), FacilityType.OUTPOST);
                SelectAndReportCardAction action3 =
                        new SelectAndReportCardAction(cardGame, thisCard.getOwner(), cardTarget, thisCard,
                                destinationFilterBlueprint);
                action3.setCardActionPrefix("1");
                action3.appendUsage(new UseOncePerTurnAction(action3, thisCard, player));
                action3.setText("Report a personnel for free");
                if (action3.canBeInitiated(cardGame))
                    actions.add(action3);

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