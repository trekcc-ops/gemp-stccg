package com.gempukku.stccg.actions.missionattempt;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.discard.RemoveDilemmaFromGameAction;
import com.gempukku.stccg.actions.modifiers.StopCardsAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.Collection;
import java.util.LinkedList;

public class FailDilemmaAction extends ActionyAction {

    private final AttemptingUnit _attemptingUnit;
    private final PhysicalCard _dilemma;
    private final EncounterSeedCardAction _encounterAction;
    private boolean _cardsStopped;
    private final boolean _discardDilemma;
    private boolean _dilemmaDiscarded;

    public FailDilemmaAction(DefaultGame cardGame, AttemptingUnit attemptingUnit,
                             PhysicalCard dilemma, EncounterSeedCardAction encounterAction, boolean discardDilemma) {
        super(cardGame, attemptingUnit.getControllerName(), ActionType.FAIL_DILEMMA);
        _attemptingUnit = attemptingUnit;
        _dilemma = dilemma;
        _encounterAction = encounterAction;
        _discardDilemma = discardDilemma;
    }

    public FailDilemmaAction(DefaultGame cardGame, AttemptingUnit attemptingUnit, PhysicalCard dilemma,
                             EncounterSeedCardAction encounterAction) {
        this(cardGame, attemptingUnit, dilemma, encounterAction, false);
    }

    public FailDilemmaAction(DefaultGame cardGame, AttemptingUnit attemptingUnit, PhysicalCard dilemma,
                             Action additionalEffect, EncounterSeedCardAction encounterAction) {
        this(cardGame, attemptingUnit, dilemma, encounterAction, false);
        appendEffect(additionalEffect);
    }


    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        if (isBeingInitiated())
            setAsInitiated();
        Action nextAction = getNextAction();
        if (nextAction != null)
            return nextAction;

        if (!_cardsStopped) {
            cardGame.sendMessage(_performingPlayerId + " failed to overcome " + _dilemma.getCardLink());
            Collection<ST1EPhysicalCard> cardsToStop = new LinkedList<>(_attemptingUnit.getAttemptingPersonnel(cardGame));
            if (_attemptingUnit instanceof ShipCard ship) {
                cardsToStop.add(ship);
            }
            _cardsStopped = true;
            return new StopCardsAction(cardGame, _performingPlayerId, cardsToStop);
        }

        if (_discardDilemma && !_dilemmaDiscarded) {
            _dilemmaDiscarded = true;
            return new RemoveDilemmaFromGameAction(cardGame, _performingPlayerId, _dilemma);
        }

        setAsFailed();
        _encounterAction.setAsFailed();
        _encounterAction.getAttemptAction().setAsFailed();
        return getNextAction();
    }
}