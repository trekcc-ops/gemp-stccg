package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.blueprints.*;
import com.gempukku.stccg.actions.choose.SelectCardAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardAction;
import com.gempukku.stccg.actions.playcard.DownloadReportableAction;
import com.gempukku.stccg.actions.targetresolver.ActionCardResolver;
import com.gempukku.stccg.actions.targetresolver.SelectCardsResolver;
import com.gempukku.stccg.actions.usage.UseNormalCardPlayAction;
import com.gempukku.stccg.actions.usage.UseOncePerGameAction;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.filters.MatchingFilterBlueprint;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.requirement.PhaseRequirement;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.YourTurnRequirement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("unused")
public class Blueprint155_021 extends CardBlueprint {

    // Attention All Hands

    public Blueprint155_021() throws InvalidCardDefinitionException {
        _playThisCardActionBlueprint = new PlayThisCardActionBlueprint(
                null, null, false, null, null, false
        );
        _actionBlueprints.add(downloadShipAction());
        _actionBlueprints.add(new SeedCardActionBlueprint(Zone.CORE, 0));
    }

    private ActionBlueprint downloadShipAction() throws InvalidCardDefinitionException {
        ActionBlueprint _dummyBlueprint = new ActivateCardActionBlueprint(
                new UsageLimitBlueprint("eachOfYourTurns", 1), new ArrayList<>(), new ArrayList<>(),
                List.of((cardGame, action, actionContext) -> new ArrayList<>())
        );
        List<Requirement> requirements = List.of(
                new PhaseRequirement(Phase.CARD_PLAY),
                new YourTurnRequirement()
        );
        SubActionBlueprint effect = (cardGame, parentAction, context) -> {
            List<Action> result = new ArrayList<>();
            String playerName = context.getPerformingPlayerId();
            Phase currentPhase = cardGame.getCurrentPhase();
            PhysicalCard thisCard = context.card();
            Player player = cardGame.getPlayer(playerName);

            Filterable downloadableCardFilter = Filters.and(CardType.SHIP, Uniqueness.UNIVERSAL, CardIcon.TNG_ICON);
            Collection<PhysicalCard> downloadableCards = Filters.filter(cardGame, downloadableCardFilter);

            if (!downloadableCards.isEmpty()) {

                SelectCardAction selectAction = new SelectVisibleCardAction(cardGame, playerName,
                        "Select a card to download", downloadableCards);
                ActionCardResolver cardTarget = new SelectCardsResolver(selectAction);
                MatchingFilterBlueprint destinationFilterBlueprint =
                        new MatchingFilterBlueprint(cardTarget, Filters.your(player), FacilityType.OUTPOST);
                DownloadReportableAction action2 =
                        new DownloadReportableAction(cardGame, playerName, cardTarget, thisCard, destinationFilterBlueprint);
                action2.setCardActionPrefix("2");
                action2.appendCost(new UseOncePerGameAction(cardGame, thisCard, player.getPlayerId(), _dummyBlueprint));
                action2.appendCost(new UseNormalCardPlayAction(cardGame, player));
                if (action2.canBeInitiated(cardGame))
                    result.add(action2);
            }
            return result;
        };
        return new ActivateCardActionBlueprint(null, requirements, new ArrayList<>(), List.of(effect));
    }

}