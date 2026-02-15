package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.choose.SelectCardAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardAction;
import com.gempukku.stccg.actions.playcard.DownloadAction;
import com.gempukku.stccg.actions.playcard.DownloadCardAction;
import com.gempukku.stccg.actions.playcard.DownloadReportableCardToDestinationAction;
import com.gempukku.stccg.actions.targetresolver.SelectCardsResolver;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ReportableCard;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.FacilityType;
import com.gempukku.stccg.filters.*;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.YouPlayerSource;

import java.util.Collection;
import java.util.Objects;

public class DownloadCardActionBlueprint extends DefaultActionBlueprint {

    String _destination;
    FilterBlueprint _filterBlueprint;
    private final boolean _specialDownload;

    @JsonCreator
    public DownloadCardActionBlueprint(@JsonProperty(value = "destination")
                                       String destination,
                                       @JsonProperty(value = "limit")
                                       UsageLimitBlueprint usageLimit,
                                       @JsonProperty(value = "filter")
                                       FilterBlueprint filterBlueprint,
                                       @JsonProperty(value = "specialDownload")
                                       boolean specialDownload
    )
            throws InvalidCardDefinitionException {
        super(new YouPlayerSource());
        if (destination.equals("yourMatchingOutpost") || destination.equals("anywhereAtThisLocation")) {
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
        _specialDownload = specialDownload;
    }

    public DownloadCardActionBlueprint(UsageLimitBlueprint usageLimit, FilterBlueprint filterBlueprint,
            boolean specialDownload) {
        super(new YouPlayerSource());
        if (usageLimit != null) {
            usageLimit.applyLimitToActionBlueprint(this);
        }
        _filterBlueprint = new AndFilterBlueprint(
                filterBlueprint,
                new YouCanDownloadFilterBlueprint()
        );
        _specialDownload = specialDownload;
    }


    @Override
    public DownloadAction createAction(DefaultGame cardGame, String performingPlayerName,
                                       PhysicalCard thisCard) {

        ActionContext actionContext = new ActionContext(thisCard, performingPlayerName);
        if (!isValid(cardGame, actionContext)) {
            return null;
        }

        CardFilter downloadableCardFilter = _filterBlueprint.getFilterable(cardGame, actionContext);
        Collection<PhysicalCard> downloadableCards = Filters.filter(cardGame, downloadableCardFilter);

        if (!downloadableCards.isEmpty() && Objects.equals(_destination, "yourMatchingOutpost")) {

            SelectCardAction selectAction = new SelectVisibleCardAction(cardGame, performingPlayerName,
                    "Select a card to download", downloadableCards);
            SelectCardsResolver cardTarget = new SelectCardsResolver(selectAction);
            MatchingFilterBlueprint destinationFilterBlueprint =
                    new MatchingFilterBlueprint(cardTarget, Filters.your(performingPlayerName), FacilityType.OUTPOST);
            DownloadReportableCardToDestinationAction action2 =
                    new DownloadReportableCardToDestinationAction(cardGame, performingPlayerName, cardTarget, thisCard, destinationFilterBlueprint);
            appendActionToContext(cardGame, action2, actionContext);
            if (action2.canBeInitiated(cardGame)) {
                return action2;
            }
        } else if (!downloadableCards.isEmpty() && _specialDownload) {
            SelectCardAction selectAction = new SelectVisibleCardAction(cardGame, performingPlayerName,
                    "Select a card to download", downloadableCards);
            SelectCardsResolver cardTarget = new SelectCardsResolver(selectAction);

            boolean allReportableCards = true;
            for (PhysicalCard card : downloadableCards) {
                if (!(card instanceof ReportableCard)) {
                    allReportableCards = false;
                }
            }

            if (allReportableCards) {
                FilterBlueprint destinationFilterBlueprint = (cardGame1, actionContext1) -> {
                    CardFilter atThisLocationFilter = Filters.atLocation(thisCard.getLocationId());
                    CardFilter shipFacilityOrPlanet = Filters.or(CardType.SHIP, CardType.FACILITY,
                            Filters.planetLocation);
                    return Filters.and(atThisLocationFilter, shipFacilityOrPlanet);
                };

                DownloadReportableCardToDestinationAction action2 =
                        new DownloadReportableCardToDestinationAction(cardGame, performingPlayerName, cardTarget, thisCard, destinationFilterBlueprint);
                appendActionToContext(cardGame, action2, actionContext);
                if (action2.canBeInitiated(cardGame)) {
                    return action2;
                }
            } else {
                DownloadCardAction action = new DownloadCardAction(cardGame, performingPlayerName, cardTarget, thisCard);
                if (action.canBeInitiated(cardGame)) {
                    return action;
                }
            }
        }

        return null;
    }

}