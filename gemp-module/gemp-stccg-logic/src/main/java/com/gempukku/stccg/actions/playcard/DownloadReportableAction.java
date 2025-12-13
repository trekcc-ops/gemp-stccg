package com.gempukku.stccg.actions.playcard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ReportableCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.filters.MatchingFilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collection;

public class DownloadReportableAction extends ActionyAction implements TopLevelSelectableAction {

    @JsonProperty("playCardAction")
    @JsonIdentityReference(alwaysAsId = true)
    private Action _playCardAction;
    private final PhysicalCard _performingCard;
    private final ActionCardResolver _cardToDownloadTarget;
    private final MatchingFilterBlueprint _destinationFilterBlueprint;

    public DownloadReportableAction(DefaultGame cardGame, Player player, ActionCardResolver cardTarget,
                                    PhysicalCard performingCard, MatchingFilterBlueprint destinationFilterBlueprint) {
        super(cardGame, player, "Download card", ActionType.DOWNLOAD_CARD);
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
            Filterable outpostFilter = _destinationFilterBlueprint.getFilterable(cardGame);
            Collection<FacilityCard> eligibleDestinations = new ArrayList<>();
            for (PhysicalCard card : Filters.filter(cardGame, outpostFilter)) {
                if (card instanceof FacilityCard facility) {
                    eligibleDestinations.add(facility);
                }
            }
            _playCardAction = new ReportCardAction(cardGame, reportable, true, eligibleDestinations);
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