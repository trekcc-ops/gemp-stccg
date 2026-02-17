package com.gempukku.stccg.actions.playcard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.targetresolver.SelectCardsResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ReportableCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SelectAndReportForFreeCardAction extends PlayCardAction {

    @JsonProperty("playCardAction")
    @JsonIdentityReference(alwaysAsId = true)
    private Action _playCardAction;
    private final PhysicalCard _performingCard;
    private final SelectCardsResolver _cardToPlayTarget;
    private final Map<PhysicalCard, Map<PhysicalCard, List<Affiliation>>> _targetMap;

    public SelectAndReportForFreeCardAction(DefaultGame cardGame, String performingPlayerName,
                                            SelectCardsResolver playableCardTarget, PhysicalCard performingCard,
                                            Map<PhysicalCard, Map<PhysicalCard, List<Affiliation>>> targetMap) {
        super(cardGame, performingCard, null, performingPlayerName, null, ActionType.PLAY_CARD);
        _cardToPlayTarget = playableCardTarget;
        _performingCard = performingCard;
        _targetMap = targetMap;
        _cardTargets.add(playableCardTarget);
    }




    protected void playCard(DefaultGame cardGame, PhysicalCard selectedCard) throws InvalidGameLogicException {
        Map<PhysicalCard, List<Affiliation>> destinationMap = _targetMap.get(selectedCard);
        Action action = new ReportCardAction(cardGame, (ReportableCard) selectedCard, true, destinationMap);
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

    @JsonIgnore
    public Collection<? extends PhysicalCard> getSelectableCardsToPlay(DefaultGame cardGame) {
        return _cardToPlayTarget.getSelectableCards(cardGame);
    }

    public void setCardReporting(PhysicalCard cardToPlay) {
        _cardToPlayTarget.setSelectedCards(List.of(cardToPlay));
    }
}