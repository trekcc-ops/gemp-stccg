package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.choose.SelectCardAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardAction;
import com.gempukku.stccg.actions.playcard.DownloadReportableCardToDestinationAction;
import com.gempukku.stccg.actions.targetresolver.ActionCardResolver;
import com.gempukku.stccg.actions.targetresolver.SelectCardsResolver;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.filters.*;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.YouPlayerSource;

import java.util.Collection;

public class DownloadCardActionBlueprint extends DefaultActionBlueprint {

    String _destination;
    FilterBlueprint _filterBlueprint;

    @JsonCreator
    public DownloadCardActionBlueprint(@JsonProperty(value = "destination")
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
            throw new InvalidCardDefinitionException("Unable to process destination other than 'yourMatchingOutpost'");
        }
        if (usageLimit != null) {
            usageLimit.applyLimitToActionBlueprint(this);
        }
        _filterBlueprint = new AndFilterBlueprint(
                filterBlueprint,
                new YouCanDownloadFilterBlueprint()
        );
    }

    @Override
    public DownloadReportableCardToDestinationAction createAction(DefaultGame cardGame, String performingPlayerName,
                                                                  PhysicalCard thisCard) {

        ActionContext actionContext = new ActionContext(thisCard, performingPlayerName);
        if (!isValid(cardGame, actionContext)) {
            return null;
        }

        CardFilter downloadableCardFilter = _filterBlueprint.getFilterable(cardGame, actionContext);
        Collection<PhysicalCard> downloadableCards = Filters.filter(cardGame, downloadableCardFilter);

        if (!downloadableCards.isEmpty() && _destination.equals("yourMatchingOutpost")) {

            SelectCardAction selectAction = new SelectVisibleCardAction(cardGame, performingPlayerName,
                    "Select a card to download", downloadableCards);
            ActionCardResolver cardTarget = new SelectCardsResolver(selectAction);
            MatchingFilterBlueprint destinationFilterBlueprint =
                    new MatchingFilterBlueprint(cardTarget, Filters.your(performingPlayerName), FacilityType.OUTPOST);
            DownloadReportableCardToDestinationAction action2 =
                    new DownloadReportableCardToDestinationAction(cardGame, performingPlayerName, cardTarget, thisCard, destinationFilterBlueprint);
            appendActionToContext(cardGame, action2, actionContext);
            if (action2.canBeInitiated(cardGame)) {
                return action2;
            }
        }

        return null;
    }

}