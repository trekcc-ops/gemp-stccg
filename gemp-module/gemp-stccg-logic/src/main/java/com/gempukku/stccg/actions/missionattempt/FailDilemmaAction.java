package com.gempukku.stccg.actions.missionattempt;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.modifiers.StopCardsAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.PlayerNotFoundException;

import java.util.Collection;
import java.util.LinkedList;

public class FailDilemmaAction extends ActionyAction {

    private final AttemptingUnit _attemptingUnit;
    private final PhysicalCard _dilemma;
    private final EncounterSeedCardAction _encounterAction;

    public FailDilemmaAction(AttemptingUnit attemptingUnit, PhysicalCard dilemma,
                             EncounterSeedCardAction encounterAction) {
        this(dilemma.getGame(), attemptingUnit.getPlayer(), attemptingUnit, dilemma, encounterAction);
    }


    public FailDilemmaAction(DefaultGame cardGame, Player performingPlayer, AttemptingUnit attemptingUnit,
                             PhysicalCard dilemma, EncounterSeedCardAction encounterAction) {
        super(cardGame, performingPlayer, ActionType.FAIL_DILEMMA);
        _attemptingUnit = attemptingUnit;
        _dilemma = dilemma;
        _encounterAction = encounterAction;
    }

    public FailDilemmaAction(DefaultGame cardGame, AttemptingUnit attemptingUnit, PhysicalCard dilemma,
                             Action additionalEffect, EncounterSeedCardAction encounterAction) {
        this(cardGame, attemptingUnit.getPlayer(), attemptingUnit, dilemma, encounterAction);
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

        if (!_wasCarriedOut) {
            cardGame.sendMessage(_performingPlayerId + " failed to overcome " + _dilemma.getCardLink());
            Collection<ST1EPhysicalCard> cardsToStop = new LinkedList<>(_attemptingUnit.getAttemptingPersonnel());
            if (_attemptingUnit instanceof PhysicalShipCard ship) {
                cardsToStop.add(ship);
            }
            _wasCarriedOut = true;
            return new StopCardsAction(cardGame, cardGame.getPlayer(_performingPlayerId), cardsToStop);
        } else {
            setAsFailed();
            _encounterAction.setAsFailed();
            _encounterAction.getAttemptAction().setAsFailed();
            return getNextAction();
        }
    }
}