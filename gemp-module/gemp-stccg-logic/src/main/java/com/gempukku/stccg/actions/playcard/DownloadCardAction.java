package com.gempukku.stccg.actions.playcard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.choose.SelectCardsFromDialogAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.*;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.google.common.collect.Iterables;

public class DownloadCardAction extends ActionyAction implements CardPerformedAction {

    @JsonProperty("playCardAction")
    @JsonIdentityReference(alwaysAsId = true)
    private Action _playCardAction;

    private final PhysicalCard _performingCard;

    private final ActionCardResolver _cardToDownloadTarget;

    public DownloadCardAction(DefaultGame cardGame, Zone fromZone, Player player, Filterable playableCardFilter,
                              PhysicalCard performingCard) {
        super(cardGame, player, "Download card from " + fromZone.getHumanReadable(),
                ActionType.DOWNLOAD_CARD);
        _cardToDownloadTarget = new SelectCardsResolver(
                new SelectCardsFromDialogAction(cardGame, player, "Select a card to download",
                        Filters.and(playableCardFilter, fromZone)));
        _performingCard = performingCard;
    }

    public DownloadCardAction(DefaultGame cardGame, Player player, ActionCardResolver cardTarget,
                              PhysicalCard performingCard) {
        super(cardGame, player, "Download card", ActionType.DOWNLOAD_CARD);
        _cardToDownloadTarget = cardTarget;
        _performingCard = performingCard;
    }


    protected void playCard(final PhysicalCard selectedCard) throws InvalidGameLogicException {
        _playCardAction = selectedCard.getPlayCardAction(true);
        selectedCard.getGame().getActionsEnvironment().addActionToStack(_playCardAction);
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
        playCard(Iterables.getOnlyElement(_cardToDownloadTarget.getCards(cardGame)));
        setAsSuccessful();
        return null;
    }

    protected Action getPlayCardAction() { return _playCardAction; }
    protected void setPlayCardAction(Action action) { _playCardAction = action; }

    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }
}