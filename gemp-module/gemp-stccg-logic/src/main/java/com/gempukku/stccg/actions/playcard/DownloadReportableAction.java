package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.targetresolver.ActionCardResolver;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ReportableCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.filters.MatchingFilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collection;

public class DownloadReportableAction extends ActionyAction implements TopLevelSelectableAction {

    private final PhysicalCard _performingCard;
    private final ActionCardResolver _cardToDownloadTarget;
    private final FilterBlueprint _destinationFilterBlueprint;


    public DownloadReportableAction(DefaultGame cardGame, String playerName, ActionCardResolver cardTarget,
                                    PhysicalCard performingCard, MatchingFilterBlueprint destinationFilterBlueprint) {
        super(cardGame, playerName, ActionType.DOWNLOAD_CARD, new ActionContext(performingCard, playerName));
        _cardToDownloadTarget = cardTarget;
        _performingCard = performingCard;
        _destinationFilterBlueprint = destinationFilterBlueprint;
        _cardTargets.add(cardTarget);
    }


    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return !_cardToDownloadTarget.cannotBeResolved(cardGame);
    }

    @Override
    protected void processEffect(DefaultGame cardGame) {
        Collection<PhysicalCard> cardsToDownload = _cardToDownloadTarget.getCards(cardGame);
        if (cardsToDownload.size() == 1 &&
                Iterables.getOnlyElement(cardsToDownload) instanceof ReportableCard reportable) {
            Filterable outpostFilter = _destinationFilterBlueprint.getFilterable(cardGame, _actionContext);
            Collection<FacilityCard> eligibleDestinations = new ArrayList<>();
            for (PhysicalCard card : Filters.filter(cardGame, outpostFilter)) {
                if (card instanceof FacilityCard facility) {
                    eligibleDestinations.add(facility);
                }
            }
            Action _playCardAction = new ReportCardAction(cardGame, reportable, true, eligibleDestinations);
            cardGame.getActionsEnvironment().addActionToStack(_playCardAction);
            setAsSuccessful();
        } else {
            cardGame.sendErrorMessage("Unable to process effect for multiple cards at once");
            setAsFailed();
        }
    }

    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

}