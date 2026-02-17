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

    @JsonCreator
    public DownloadCardToDestinationActionBlueprint(@JsonProperty(value = "destination")
                                       DestinationBlueprint destination,
                                                    @JsonProperty(value = "limit")
                                       UsageLimitBlueprint usageLimit,
                                                    @JsonProperty(value = "filter")
                                       FilterBlueprint filterBlueprint,
                                                    @JsonProperty(value = "specialDownload")
                                       boolean specialDownload,
                                                    @JsonProperty(value = "inPlaceOfNormalCardPlay")
                                       boolean inPlaceOfNormalCardPlay
    ) {
        super(new YouPlayerSource());
        _destinationBlueprint = destination;
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
        super(new YouPlayerSource());
        if (usageLimit != null) {
            usageLimit.applyLimitToActionBlueprint(this);
        }
        _filterBlueprint = new AndFilterBlueprint(
                filterBlueprint,
                new YouCanDownloadFilterBlueprint()
        );
        _specialDownload = specialDownload;
        _destinationBlueprint = null;
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

        if (_destinationBlueprint != null && cardGame instanceof ST1EGame stGame) {

            Map<PhysicalCard, Map<PhysicalCard, List<Affiliation>>> targetMap = new HashMap<>();
            for (PhysicalCard card : downloadableCards) {
                if (card instanceof ReportableCard reportable) {
                    Collection<PhysicalCard> destinations =
                            _destinationBlueprint.getDestinationOptions(stGame, performingPlayerName, card, actionContext);
                    Map<PhysicalCard, List<Affiliation>> destinationMap = stGame.getRules()
                            .getDestinationAndAffiliationMapForReportingCard(reportable, stGame, destinations, true);
                    targetMap.put(card, destinationMap);
                }
            }
            SelectCardAction selectAction = new SelectVisibleCardAction(cardGame, performingPlayerName,
                    "Select a card to download", targetMap.keySet());
            SelectCardsResolver cardTarget = new SelectCardsResolver(selectAction);
            DownloadReportableCardToDestinationAction downloadAction =
                    new DownloadReportableCardToDestinationAction(cardGame, performingPlayerName, cardTarget,
                            thisCard, targetMap);
            appendActionToContext(cardGame, downloadAction, actionContext);
            if (downloadAction.canBeInitiated(cardGame)) {
                return downloadAction;
            }
        } else if (!downloadableCards.isEmpty() && _specialDownload && cardGame instanceof ST1EGame stGame) {
            SelectCardAction selectAction = new SelectVisibleCardAction(cardGame, performingPlayerName,
                    "Select a card to download", downloadableCards);
            SelectCardsResolver cardTarget = new SelectCardsResolver(selectAction);

            boolean allReportableCards = true;
            boolean allNotReportableCards = true;
            for (PhysicalCard card : downloadableCards) {
                if (!(card instanceof ReportableCard)) {
                    allReportableCards = false;
                } else {
                    allNotReportableCards = false;
                }
            }

            Collection<PhysicalCard> destinationOptions = Filters.filterCardsInPlay(cardGame,
                Filters.atLocation(thisCard.getLocationId()),
                Filters.or(CardType.SHIP, CardType.FACILITY, Filters.planetLocation)
            );


            Map<PhysicalCard, Map<PhysicalCard, List<Affiliation>>> targetMap = new HashMap<>();
            for (PhysicalCard card : downloadableCards) {
                if (card instanceof ReportableCard reportable) {
                    Map<PhysicalCard, List<Affiliation>> destinationMap = stGame.getRules()
                            .getDestinationAndAffiliationMapForReportingCard(reportable, stGame, destinationOptions, true);
                    targetMap.put(card, destinationMap);
                }
            }


            if (allReportableCards) {
                DownloadReportableCardToDestinationAction action2 =
                        new DownloadReportableCardToDestinationAction(
                                cardGame, performingPlayerName, cardTarget, thisCard, targetMap);
                appendActionToContext(cardGame, action2, actionContext);
                if (action2.canBeInitiated(cardGame)) {
                    return action2;
                }
            } else if (allNotReportableCards) {
                DownloadCardAction action = new DownloadCardAction(cardGame, performingPlayerName, cardTarget, thisCard);
                if (action.canBeInitiated(cardGame)) {
                    return action;
                }
            }
        }

        return null;
    }

}