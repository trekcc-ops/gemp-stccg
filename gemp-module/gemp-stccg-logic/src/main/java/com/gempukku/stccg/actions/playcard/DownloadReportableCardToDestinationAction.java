package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.targetresolver.SelectCardsResolver;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ReportableCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DownloadReportableCardToDestinationAction extends ActionWithSubActions implements DownloadAction {

    private final PhysicalCard _performingCard;
    private final SelectCardsResolver _cardToDownloadTarget;
    private final FilterBlueprint _destinationFilterBlueprint;


    public DownloadReportableCardToDestinationAction(DefaultGame cardGame, String playerName,
                                                     SelectCardsResolver cardTarget, PhysicalCard performingCard,
                                                     FilterBlueprint destinationFilterBlueprint) {
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
            Collection<PhysicalCard> eligibleDestinations = new ArrayList<>(Filters.filter(cardGame, outpostFilter));
            Action _playCardAction = new ReportCardAction(cardGame, reportable, true, eligibleDestinations, true);
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

    @Override
    public Collection<? extends PhysicalCard> getDownloadableTargets(DefaultGame cardGame) {
        return _cardToDownloadTarget.getSelectableCards(cardGame);
    }

    @Override
    public void setCardToDownload(PhysicalCard cardToDownload) {
        _cardToDownloadTarget.setSelectedCards(List.of(cardToDownload));
    }
}