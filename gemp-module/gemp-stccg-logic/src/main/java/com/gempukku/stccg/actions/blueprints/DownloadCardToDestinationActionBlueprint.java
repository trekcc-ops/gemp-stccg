package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.choose.SelectCardAction;
import com.gempukku.stccg.actions.choose.SelectVisibleCardAction;
import com.gempukku.stccg.actions.playcard.DownloadAction;
import com.gempukku.stccg.actions.playcard.DownloadCardAction;
import com.gempukku.stccg.actions.playcard.DownloadReportableCardToDestinationAction;
import com.gempukku.stccg.actions.targetresolver.EnterPlayAtDestinationResolver;
import com.gempukku.stccg.actions.targetresolver.SelectCardsResolver;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ReportableCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.filters.*;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.player.YouPlayerSource;
import com.gempukku.stccg.requirement.PhaseRequirement;
import com.gempukku.stccg.requirement.YourTurnRequirement;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DownloadCardToDestinationActionBlueprint extends DefaultActionBlueprint {

    FilterBlueprint _filterBlueprint;
    private final boolean _specialDownload;
    private final DestinationBlueprint _destinationBlueprint;
    private final FilterBlueprint _destinationFilter;

    @JsonCreator
    @SuppressWarnings("unused") // Used in JSON deserialization
    private DownloadCardToDestinationActionBlueprint(@JsonProperty(value = "destination")
                                       DestinationBlueprint destination,
                                                    @JsonProperty(value = "limit")
                                       UsageLimitBlueprint usageLimit,
                                                    @JsonProperty(value = "filter")
                                       FilterBlueprint filterBlueprint,
                                                    @JsonProperty(value = "destinationFilter")
                                                    FilterBlueprint destinationFilter,
                                                    @JsonProperty(value = "specialDownload")
                                       boolean specialDownload,
                                                    @JsonProperty(value = "inPlaceOfNormalCardPlay")
                                       boolean inPlaceOfNormalCardPlay
    ) {
        super(new YouPlayerSource());
        _destinationBlueprint = destination;
        _destinationFilter = destinationFilter;
        if (usageLimit != null) {
            usageLimit.applyLimitToActionBlueprint(this);
        }
        _filterBlueprint = new AndFilterBlueprint(
                filterBlueprint,
                new YouCanDownloadFilterBlueprint()
        );
        _specialDownload = specialDownload;
        if (inPlaceOfNormalCardPlay) {
            costs.add(new UseNormalCardPlayBlueprint());
            _requirements.add(new PhaseRequirement(Phase.CARD_PLAY));
            _requirements.add(new YourTurnRequirement());
        }
    }

    public DownloadCardToDestinationActionBlueprint(UsageLimitBlueprint usageLimit, FilterBlueprint filterBlueprint,
                                                    boolean specialDownload) {
        this(null, usageLimit, filterBlueprint, null, specialDownload, false);
    }


    @Override
    public DownloadAction createAction(DefaultGame cardGame, String performingPlayerName,
                                       PhysicalCard thisCard) {

        GameTextContext actionContext = new GameTextContext(thisCard, performingPlayerName);
        if (!isValid(cardGame, actionContext)) {
            return null;
        }

        CardFilter downloadableCardFilter = _filterBlueprint.getFilterable(cardGame, actionContext);
        Collection<PhysicalCard> downloadableCards = Filters.filter(cardGame, downloadableCardFilter);

        if (downloadableCards.isEmpty()) {
            return null;
        }

        boolean allReportableCards = true;
        boolean allNotReportableCards = true;
        for (PhysicalCard card : downloadableCards) {
            if (!(card instanceof ReportableCard)) {
                allReportableCards = false;
            } else {
                allNotReportableCards = false;
            }
        }

        if (allNotReportableCards) {
            Map<PhysicalCard, Collection<PhysicalCard>> destinationTargetMap = new HashMap<>();
            for (PhysicalCard card : downloadableCards) {
                if (_specialDownload) {
                    Collection<PhysicalCard> destinationOptions = Filters.filter(
                            card.getDestinationOptionsFromGameText(actionContext, cardGame),
                            cardGame,
                            Filters.or(
                                    Filters.atLocation(actionContext.card().getLocationId()),
                                    Filters.isCoreProxy
                            )
                    );
                    if (!destinationOptions.isEmpty()) {
                        destinationTargetMap.put(card, destinationOptions);
                    }
                } else if (_destinationFilter != null) {
                    CardFilter destinationFilter = _destinationFilter.getFilterable(cardGame, actionContext);
                    Collection<PhysicalCard> destinationOptions = Filters.filter(
                            card.getDestinationOptionsFromGameText(actionContext, cardGame),
                            cardGame, destinationFilter
                    );
                    if (!destinationOptions.isEmpty()) {
                        destinationTargetMap.put(card, destinationOptions);
                    }
                }
            }
            if (!destinationTargetMap.isEmpty()) {
                EnterPlayAtDestinationResolver resolver =
                        new EnterPlayAtDestinationResolver(performingPlayerName, destinationTargetMap);
                DownloadCardAction action =
                        new DownloadCardAction(cardGame, performingPlayerName, resolver, thisCard, actionContext);
                appendSubActions(action);
                if (action.canBeInitiated(cardGame)) {
                    return action;
                }
            }
        } else if (_destinationBlueprint != null && cardGame instanceof ST1EGame stGame) {

            Map<PhysicalCard, Map<PhysicalCard, List<Affiliation>>> targetMap = new HashMap<>();
            for (PhysicalCard card : downloadableCards) {
                if (card instanceof ReportableCard reportable) {
                    Collection<PhysicalCard> destinations = _destinationBlueprint
                            .getDestinationOptions(stGame, performingPlayerName, card, actionContext);
                    Map<PhysicalCard, List<Affiliation>> destinationMap = stGame.getRules()
                            .getDestinationAndAffiliationMapForReportingCard(reportable, stGame, destinations,
                                    true, downloadableCardFilter);
                    if (!destinationMap.isEmpty()) {
                        targetMap.put(card, destinationMap);
                    }
                }
            }
            if (!targetMap.isEmpty()) {
                SelectCardAction selectAction = new SelectVisibleCardAction(cardGame, performingPlayerName,
                        "Select a card to download", targetMap.keySet());
                SelectCardsResolver cardTarget = new SelectCardsResolver(selectAction);
                DownloadReportableCardToDestinationAction downloadAction =
                        new DownloadReportableCardToDestinationAction(cardGame, performingPlayerName, cardTarget,
                                thisCard, targetMap);
                appendSubActions(downloadAction);
                if (downloadAction.canBeInitiated(cardGame)) {
                    return downloadAction;
                }
            }
        } else if (_specialDownload && cardGame instanceof ST1EGame stGame && allReportableCards) {

            Collection<PhysicalCard> destinationOptions = Filters.filterCardsInPlay(cardGame,
                    Filters.atLocation(thisCard.getLocationId()),
                    Filters.or(CardType.SHIP, CardType.FACILITY, Filters.planetLocation)
            );
            SelectCardAction selectAction = new SelectVisibleCardAction(cardGame, performingPlayerName,
                    "Select a card to download", downloadableCards);
            SelectCardsResolver cardTarget = new SelectCardsResolver(selectAction);

            Map<PhysicalCard, Map<PhysicalCard, List<Affiliation>>> targetMap = new HashMap<>();
            for (PhysicalCard card : downloadableCards) {
                if (card instanceof ReportableCard reportable) {
                    Map<PhysicalCard, List<Affiliation>> destinationMap = stGame.getRules()
                            .getDestinationAndAffiliationMapForReportingCard(reportable, stGame,
                                    destinationOptions, true);
                    targetMap.put(card, destinationMap);
                }
            }
            DownloadReportableCardToDestinationAction action2 = new DownloadReportableCardToDestinationAction(
                    cardGame, performingPlayerName, cardTarget, thisCard, targetMap);
            appendSubActions(action2);
            if (action2.canBeInitiated(cardGame)) {
                return action2;
            }
        }

        return null;
    }

}