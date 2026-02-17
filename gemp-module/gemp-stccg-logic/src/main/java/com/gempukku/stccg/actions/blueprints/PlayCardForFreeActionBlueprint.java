package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.choose.SelectCardAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardAction;
import com.gempukku.stccg.actions.playcard.EnterPlayActionType;
import com.gempukku.stccg.actions.playcard.PlayCardAction;
import com.gempukku.stccg.actions.playcard.SelectAndReportForFreeCardAction;
import com.gempukku.stccg.actions.targetresolver.SelectCardsResolver;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ReportableCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.filters.*;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.player.YouPlayerSource;
import com.gempukku.stccg.requirement.PhaseRequirement;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.YourTurnRequirement;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayCardForFreeActionBlueprint extends DefaultActionBlueprint {

    private final DestinationBlueprint _destinationBlueprint;
    FilterBlueprint _filterBlueprint;

    @JsonCreator
    public PlayCardForFreeActionBlueprint(@JsonProperty(value = "destination", required = true)
                                       DestinationBlueprint destinationBlueprint,
                                          @JsonProperty(value = "limit")
                                       UsageLimitBlueprint usageLimit,
                                          @JsonProperty(value = "filter")
                                          FilterBlueprint filterBlueprint,
                                          @JsonProperty("requires")
                                          Requirement requirement
    ) {
        super(new YouPlayerSource());
        _destinationBlueprint = destinationBlueprint;
        if (usageLimit != null) {
            usageLimit.applyLimitToActionBlueprint(this);
        }
        _filterBlueprint = filterBlueprint;
        _requirements.addAll(List.of(new PhaseRequirement(Phase.CARD_PLAY), new YourTurnRequirement()));
        if (requirement != null) {
            _requirements.add(requirement);
        }
    }

    @Override
    public PlayCardAction createAction(DefaultGame cardGame, String performingPlayerName,
                                                 PhysicalCard thisCard) {
        ActionContext actionContext = new ActionContext(thisCard, performingPlayerName);
        if (!isValid(cardGame, actionContext)) {
            return null;
        }

        CardFilter playableCardFilter = Filters.and(
                _filterBlueprint.getFilterable(cardGame, actionContext),
                new InYourHandFilter(performingPlayerName),
                new CanEnterPlayFilter(EnterPlayActionType.PLAY)
        );

        Collection<PhysicalCard> playableCards = Filters.filter(cardGame, playableCardFilter);

        if (!playableCards.isEmpty() && cardGame instanceof ST1EGame stGame) {
            Map<PhysicalCard, Map<PhysicalCard, List<Affiliation>>> targetMap = new HashMap<>();
            for (PhysicalCard card : playableCards) {
                if (card instanceof ReportableCard reportable) {
                    Collection<PhysicalCard> destinations =
                            _destinationBlueprint.getDestinationOptions(stGame, performingPlayerName, card, actionContext);
                    Map<PhysicalCard, List<Affiliation>> destinationMap = stGame.getRules()
                            .getDestinationAndAffiliationMapForReportingCard(reportable, stGame, destinations, true,
                                    playableCardFilter);
                    targetMap.put(card, destinationMap);
                }
            }

            SelectCardAction selectAction = new SelectVisibleCardAction(cardGame, performingPlayerName,
                    "Select a card to play", targetMap.keySet());
            SelectCardsResolver cardTarget = new SelectCardsResolver(selectAction);

            SelectAndReportForFreeCardAction reportAction =
                    new SelectAndReportForFreeCardAction(cardGame, actionContext.card().getOwnerName(), cardTarget, actionContext.card(),
                            targetMap);
            appendActionToContext(cardGame, reportAction, actionContext);
            if (reportAction.canBeInitiated(cardGame)) {
                return reportAction;
            }
        }
        return null;
    }

}