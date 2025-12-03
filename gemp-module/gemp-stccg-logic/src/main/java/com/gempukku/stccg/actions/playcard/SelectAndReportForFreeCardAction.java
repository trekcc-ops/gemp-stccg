package com.gempukku.stccg.actions.playcard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalReportableCard1E;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.filters.MatchingFilterBlueprint;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.google.common.collect.Iterables;

import java.util.Collection;

public class SelectAndReportForFreeCardAction extends ActionyAction implements TopLevelSelectableAction {

    @JsonProperty("playCardAction")
    @JsonIdentityReference(alwaysAsId = true)
    private Action _playCardAction;
    private final PhysicalCard _performingCard;
    private final ActionCardResolver _cardToPlayTarget;
    private final MatchingFilterBlueprint _destinationFilterBlueprint;

    public SelectAndReportForFreeCardAction(DefaultGame cardGame, String performingPlayerName,
                                            ActionCardResolver playableCardTarget, PhysicalCard performingCard,
                                            MatchingFilterBlueprint destinationFilterBlueprint) {
        super(cardGame, performingPlayerName, "Report card from hand", ActionType.PLAY_CARD);
        _cardToPlayTarget = playableCardTarget;
        _performingCard = performingCard;
        _destinationFilterBlueprint = destinationFilterBlueprint;
    }



    protected void playCard(DefaultGame cardGame, PhysicalCard selectedCard) throws InvalidGameLogicException {
        Filterable outpostFilter = _destinationFilterBlueprint.getFilterable(cardGame);
        Collection<PhysicalCard> eligibleDestinations = Filters.filter(cardGame, outpostFilter);

        Action action = new ReportCardAction(cardGame, (PhysicalReportableCard1E) selectedCard,
                true, eligibleDestinations);
        setPlayCardAction(action);
        cardGame.getActionsEnvironment().addActionToStack(getPlayCardAction());
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
        playCard(cardGame, Iterables.getOnlyElement(_cardToPlayTarget.getCards(cardGame)));
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