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
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.FacilityType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.filters.*;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.YouPlayerSource;
import com.gempukku.stccg.requirement.PhaseRequirement;
import com.gempukku.stccg.requirement.YourTurnRequirement;

import java.util.Collection;
import java.util.List;

public class PlayCardForFreeActionBlueprint extends DefaultActionBlueprint {

    String _destination;
    FilterBlueprint _filterBlueprint;

    @JsonCreator
    public PlayCardForFreeActionBlueprint(@JsonProperty(value = "destination")
                                       String destination,
                                          @JsonProperty(value = "limit")
                                       UsageLimitBlueprint usageLimit,
                                          @JsonProperty(value = "filter")
                                          FilterBlueprint filterBlueprint
    )
            throws InvalidCardDefinitionException {
        super(new YouPlayerSource());
        if (destination.equals("yourMatchingOutpost")) {
            _destination = destination;
        } else {
            throw new InvalidCardDefinitionException("No definition if not reporting to your matching outpost");
        }
        if (usageLimit != null) {
            usageLimit.applyLimitToActionBlueprint(this);
        }
        _filterBlueprint = filterBlueprint;
        _requirements.addAll(List.of(new PhaseRequirement(Phase.CARD_PLAY), new YourTurnRequirement()));
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
        if (!playableCards.isEmpty()) {

            SelectCardAction selectAction = new SelectVisibleCardAction(cardGame, performingPlayerName,
                    "Select a card to report", playableCards);
            SelectCardsResolver cardTarget = new SelectCardsResolver(selectAction);

            if (_destination.equals("yourMatchingOutpost")) {
                MatchingFilterBlueprint destinationFilterBlueprint =
                        new MatchingFilterBlueprint(cardTarget, Filters.your(performingPlayerName), FacilityType.OUTPOST);
                SelectAndReportForFreeCardAction action1 =
                        new SelectAndReportForFreeCardAction(cardGame, actionContext.card().getOwnerName(), cardTarget, actionContext.card(),
                                destinationFilterBlueprint);
                appendActionToContext(cardGame, action1, actionContext);
                if (action1.canBeInitiated(cardGame)) {
                    return action1;
                }
            }
        }
        return null;
    }

}