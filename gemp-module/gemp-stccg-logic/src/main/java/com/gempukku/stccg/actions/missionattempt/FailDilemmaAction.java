package com.gempukku.stccg.actions.missionattempt;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;

import java.util.Collection;
import java.util.LinkedList;

public class FailDilemmaAction extends ActionyAction {

    private final AttemptingUnit _attemptingUnit;
    private final PhysicalCard _dilemma;

    public FailDilemmaAction(AttemptingUnit attemptingUnit, PhysicalCard dilemma,
                             EncounterSeedCardAction encounterAction) {
        this(attemptingUnit.getPlayer(), attemptingUnit, dilemma, encounterAction);
    }

    public FailDilemmaAction(Player performingPlayer, AttemptingUnit attemptingUnit, PhysicalCard dilemma,
                             EncounterSeedCardAction encounterAction) {
        super(performingPlayer, ActionType.FAIL_DILEMMA);
        _attemptingUnit = attemptingUnit;
        _dilemma = dilemma;
    }

    @Override
    public PhysicalCard getActionSource() {
        return _dilemma;
    }

    @Override
    public PhysicalCard getCardForActionSelection() {
        return _dilemma;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        if (!_wasCarriedOut) {
            cardGame.sendMessage(_performingPlayerId + " failed to overcome " + _dilemma.getCardLink());
            Collection<ST1EPhysicalCard> cardsToStop = new LinkedList<>(_attemptingUnit.getAttemptingPersonnel());
            if (_attemptingUnit instanceof PhysicalShipCard ship) {
                cardsToStop.add(ship);
            }
            _wasCarriedOut = true;
            return new StopCardsAction(cardGame.getPlayer(_performingPlayerId), cardsToStop);
        } else {
            return getNextAction();
        }
    }
}