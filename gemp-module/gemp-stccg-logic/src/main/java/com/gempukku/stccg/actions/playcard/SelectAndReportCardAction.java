package com.gempukku.stccg.actions.playcard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.choose.SelectCardsFromDialogAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.google.common.collect.Iterables;

public abstract class SelectAndReportCardAction extends ActionyAction implements TopLevelSelectableAction {

    @JsonProperty("playCardAction")
    @JsonIdentityReference(alwaysAsId = true)
    private Action _playCardAction;
    private final PhysicalCard _performingCard;
    private final ActionCardResolver _cardToPlayTarget;

    public SelectAndReportCardAction(DefaultGame cardGame, Player player, ActionCardResolver cardToPlayTarget,
                                     PhysicalCard performingCard) {
        super(cardGame, player, "Report card from hand", ActionType.PLAY_CARD);
        _cardToPlayTarget = cardToPlayTarget;
        _performingCard = performingCard;
    }

    public SelectAndReportCardAction(DefaultGame cardGame, Player player, Filterable playableCardFilter,
                                     PhysicalCard performingCard) {
        super(cardGame, player, "Report card from hand", ActionType.PLAY_CARD);
        _cardToPlayTarget = new SelectCardsResolver(
                new SelectCardsFromDialogAction(cardGame, player, "Select a card to report",
                        Filters.and(playableCardFilter, Zone.HAND)));
        _performingCard = performingCard;
    }


    abstract protected void playCard(PhysicalCard selectedCard) throws InvalidGameLogicException;

    @Override
    public boolean wasCarriedOut() {
        if (_playCardAction == null)
            return false;
        if (_playCardAction instanceof PlayCardAction)
            return _playCardAction.wasCarriedOut();
        return true;
    }

    public int getCardIdForActionSelection() {
        return _performingCard.getCardId();
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return !_cardToPlayTarget.willProbablyBeEmpty(cardGame);
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        Action nextCost = getNextCost();
        if (nextCost != null)
            return nextCost;

        if (!_cardToPlayTarget.isResolved()) {
            if (_cardToPlayTarget instanceof SelectCardsResolver selectTarget) {
                if (selectTarget.getSelectionAction().wasCompleted()) {
                    _cardToPlayTarget.resolve(cardGame);
                } else {
                    return selectTarget.getSelectionAction();
                }
            } else {
                _cardToPlayTarget.resolve(cardGame);
            }
        }

        // The playCard method determines valid destinations
        playCard(Iterables.getOnlyElement(_cardToPlayTarget.getCards(cardGame)));
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