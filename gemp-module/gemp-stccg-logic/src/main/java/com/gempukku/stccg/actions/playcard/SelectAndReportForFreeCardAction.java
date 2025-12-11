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
        _cardTargets.add(playableCardTarget);
    }



    protected void playCard(DefaultGame cardGame, PhysicalCard selectedCard) throws InvalidGameLogicException {
        Filterable outpostFilter = _destinationFilterBlueprint.getFilterable(cardGame);
        Collection<PhysicalCard> eligibleDestinations = Filters.filter(cardGame, outpostFilter);
        Action action = new ReportCardAction(cardGame, (ReportableCard) selectedCard,
                true, eligibleDestinations);
        setPlayCardAction(action);
        cardGame.getActionsEnvironment().addActionToStack(getPlayCardAction());
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return !_cardToPlayTarget.cannotBeResolved(cardGame);
    }

    @Override
    public void processEffect(DefaultGame cardGame) {
        try {
            // The playCard method determines valid destinations
            playCard(cardGame, Iterables.getOnlyElement(_cardToPlayTarget.getCards(cardGame)));
            setAsSuccessful();
        } catch(InvalidGameLogicException exp) {
            cardGame.sendErrorMessage(exp);
            setAsFailed();
        }
    }

    protected Action getPlayCardAction() { return _playCardAction; }
    protected void setPlayCardAction(Action action) { _playCardAction = action; }

    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }
}