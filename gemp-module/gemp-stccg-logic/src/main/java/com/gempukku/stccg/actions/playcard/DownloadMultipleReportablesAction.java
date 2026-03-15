package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.targetresolver.ReportMultipleCardsResolver;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ReportableCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.ModifierFlag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DownloadMultipleReportablesAction extends ActionyAction implements DownloadAction {
    private final Zone _fromZone;
    private final PhysicalCard _performingCard;
    private final ReportMultipleCardsResolver _resolver;

    public DownloadMultipleReportablesAction(DefaultGame cardGame, Zone fromZone,
                                             String performingPlayerName,
                                             PhysicalCard actionSource,
                                             ReportMultipleCardsResolver resolver) {
        super(cardGame, performingPlayerName, ActionType.DOWNLOAD_CARD);
        _resolver = resolver;
        _cardTargets.add(_resolver);
        _performingCard = actionSource;
        _fromZone = fromZone;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        if (_resolver.cannotBeResolved(cardGame)) {
            return false;
        } else if (_fromZone == Zone.DISCARD || _fromZone == Zone.DRAW_DECK) {
            return !cardGame.hasFlagActive(ModifierFlag.CANT_PLAY_FROM_DISCARD_OR_DECK);
        } else {
            return true;
        }
    }

    protected void processEffect(DefaultGame cardGame) {
        List<PhysicalCard> _cardsToDownload = new ArrayList<>(_resolver.getCardsToDownload());
        FacilityCard _destination = _resolver.getDestinationFacility();
            // iterate backwards so the stacked actions will be executed in the right order
        for (int i = _cardsToDownload.size() - 1; i >= 0; i--) {
            PhysicalCard card = _cardsToDownload.get(i);
            if (card instanceof ReportableCard reportable) {
                ReportCardAction playCardAction =
                        new ReportCardAction(cardGame, reportable, true, true, _performingCard);
                playCardAction.setDestination(_destination);
                cardGame.addActionToStack(playCardAction);
            } else {
                cardGame.sendErrorMessage("Could not report card because it was not a reportable card type");
            }
        }
        setAsSuccessful();
    }

    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

    @Override
    public Collection<? extends PhysicalCard> getDownloadableTargets(DefaultGame cardGame) {
        return _resolver.getSelectableCardsToPlay();
    }

    @Override
    public void selectCardToDownload(PhysicalCard cardToDownload) {
        _resolver.setCardsToPlay(List.of(cardToDownload));
    }
}