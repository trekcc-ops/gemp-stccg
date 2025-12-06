package com.gempukku.stccg.actions.playcard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ReportableCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.filters.MatchingFilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.google.common.collect.Iterables;

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
    }


    protected void playCard(DefaultGame cardGame, PhysicalCard selectedCard) throws InvalidGameLogicException {
        Filterable outpostFilter = _destinationFilterBlueprint.getFilterable(cardGame);
        Collection<PhysicalCard> eligibleDestinations = Filters.filter(cardGame, outpostFilter);

        _playCardAction = new ReportCardAction(cardGame, (ReportableCard) selectedCard,
                true, eligibleDestinations);
        cardGame.getActionsEnvironment().addActionToStack(_playCardAction);
    }

    @Override
    public boolean wasCarriedOut() {
        if (_playCardAction == null)
            return false;
        if (_playCardAction instanceof PlayCardAction)
            return _playCardAction.wasCarriedOut();
        return true;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return !_cardToDownloadTarget.willProbablyBeEmpty(cardGame);
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        Action nextCost = getNextCost();
        if (nextCost != null)
            return nextCost;

        if (!_cardToDownloadTarget.isResolved()) {
            if (_cardToDownloadTarget instanceof SelectCardsResolver selectTarget) {
                if (selectTarget.getSelectionAction().wasCompleted()) {
                    _cardToDownloadTarget.resolve(cardGame);
                } else {
                    return selectTarget.getSelectionAction();
                }
            } else {
                _cardToDownloadTarget.resolve(cardGame);
            }
        }

        // The playCard method determines valid destinations
        playCard(cardGame, Iterables.getOnlyElement(_cardToDownloadTarget.getCards(cardGame)));
        setAsSuccessful();
        return null;
    }

    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

}