package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.targetresolver.SelectCardsResolver;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ReportableCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.game.DefaultGame;
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DownloadReportableCardToDestinationAction extends ActionWithSubActions implements DownloadAction {

    private final PhysicalCard _performingCard;
    private final SelectCardsResolver _cardToDownloadTarget;
    private final Map<PhysicalCard, Map<PhysicalCard, List<Affiliation>>> _targetMap;


    public DownloadReportableCardToDestinationAction(DefaultGame cardGame, String playerName,
                                                     SelectCardsResolver cardTarget, PhysicalCard performingCard,
                                                     Map<PhysicalCard, Map<PhysicalCard, List<Affiliation>>> targetMap) {
        super(cardGame, playerName, ActionType.DOWNLOAD_CARD, new ActionContext(performingCard, playerName));
        _cardToDownloadTarget = cardTarget;
        _performingCard = performingCard;
        _targetMap = targetMap;
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
            Map<PhysicalCard, List<Affiliation>> destinationMap = _targetMap.get(reportable);
            Action _playCardAction = new ReportCardAction(cardGame, reportable, true, destinationMap);
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