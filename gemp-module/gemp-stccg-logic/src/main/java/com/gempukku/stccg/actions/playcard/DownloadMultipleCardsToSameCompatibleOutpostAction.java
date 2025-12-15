package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.targetresolver.DownloadMultipleCardsResolver;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.ModifierFlag;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DownloadMultipleCardsToSameCompatibleOutpostAction extends ActionyAction
        implements TopLevelSelectableAction {
    private final Zone _fromZone;
    private final PhysicalCard _performingCard;
    private final Map<PersonnelCard, List<PersonnelCard>> _validCombinations;
    private final DownloadMultipleCardsResolver _resolver;

    public DownloadMultipleCardsToSameCompatibleOutpostAction(DefaultGame cardGame, Zone fromZone,
                                                              String performingPlayerName,
                                                              PhysicalCard actionSource,
                                                              Map<PersonnelCard, List<PersonnelCard>> validCombinations,
                                                              int maxCardCount) {
        super(cardGame, performingPlayerName, ActionType.DOWNLOAD_CARD);
        _resolver = new DownloadMultipleCardsResolver(validCombinations, maxCardCount, _performingPlayerId);
        _cardTargets.add(_resolver);
        _performingCard = actionSource;
        _validCombinations = validCombinations;
        _fromZone = fromZone;
    }

    protected Collection<PhysicalCard> getPlayableCards() {
        return new LinkedList<>(_validCombinations.keySet());
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        if (getPlayableCards().isEmpty()) {
            return false;
        } else if (_fromZone == Zone.DISCARD || _fromZone == Zone.DRAW_DECK) {
            return !cardGame.hasFlagActive(ModifierFlag.CANT_PLAY_FROM_DISCARD_OR_DECK);
        } else {
            return true;
        }
    }

    protected void processEffect(DefaultGame cardGame) {
        Collection<PhysicalCard> _cardsToDownload = _resolver.getCardsToDownload();
        FacilityCard _destination = _resolver.getDestinationFacility();
        for (PhysicalCard card : _cardsToDownload) {
            Action playCardAction = card.getPlayCardAction(cardGame, true);
            if (playCardAction instanceof ReportCardAction reportAction) {
                reportAction.setDestination(_destination);
                cardGame.addActionToStack(playCardAction);
            }
        }
        setAsSuccessful();
    }

    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

}