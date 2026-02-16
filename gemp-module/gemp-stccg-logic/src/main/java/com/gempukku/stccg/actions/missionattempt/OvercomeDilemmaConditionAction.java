package com.gempukku.stccg.actions.missionattempt;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.discard.RemoveDilemmaFromGameAction;
import com.gempukku.stccg.actions.modifiers.StopCardsAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.condition.missionrequirements.MissionRequirement;
import com.gempukku.stccg.game.DefaultGame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class OvercomeDilemmaConditionAction extends ActionyAction {

    private final AttemptingUnit _attemptingUnit;
    private final EncounterSeedCardAction _encounterAction;
    private final MissionRequirement _conditions;
    private boolean _conditionsChecked;
    private boolean _conditionsMet;
    private final boolean _discardDilemma;
    private final PhysicalCard _dilemma;
    private boolean _cardsStopped;
    private final List<Action> _additionalFailActions;
    private boolean _dilemmaDiscarded;

    public OvercomeDilemmaConditionAction(DefaultGame cardGame, PhysicalCard dilemma,
                                          EncounterSeedCardAction encounterAction, MissionRequirement conditions,
                                          AttemptingUnit attemptingUnit, List<Action> failDilemmaActions, boolean discardDilemma) {
        super(cardGame, attemptingUnit.getControllerName(), ActionType.OVERCOME_DILEMMA);
        _dilemma = dilemma;
        _attemptingUnit = attemptingUnit;
        _additionalFailActions = failDilemmaActions;
        _conditions = conditions;
        _encounterAction = encounterAction;
        _discardDilemma = discardDilemma;
    }


    public OvercomeDilemmaConditionAction(DefaultGame cardGame, PhysicalCard dilemma,
                                          EncounterSeedCardAction encounterAction, MissionRequirement conditions,
                                          AttemptingUnit attemptingUnit, Action failDilemmaAction, boolean discardDilemma) {
        super(cardGame, attemptingUnit.getControllerName(), ActionType.OVERCOME_DILEMMA);
        _dilemma = dilemma;
        _attemptingUnit = attemptingUnit;
        _additionalFailActions = new ArrayList<>(List.of(failDilemmaAction));
        _conditions = conditions;
        _encounterAction = encounterAction;
        _discardDilemma = discardDilemma;
    }

    public OvercomeDilemmaConditionAction(DefaultGame cardGame, PhysicalCard dilemma,
                                          EncounterSeedCardAction encounterAction, MissionRequirement conditions,
                                          AttemptingUnit attemptingUnit, Action failDilemmaAction) {
        this(cardGame, dilemma, encounterAction, conditions, attemptingUnit, failDilemmaAction, false);
    }

    public OvercomeDilemmaConditionAction(DefaultGame cardGame, PhysicalCard dilemma, EncounterSeedCardAction action,
                                          MissionRequirement conditions, AttemptingUnit attemptingUnit) {
        this(cardGame, dilemma, action, conditions, attemptingUnit, new ArrayList<>(), false);
    }


    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    protected void processEffect(DefaultGame cardGame) {
        if (!_conditionsChecked) {
            _conditionsChecked = true;
            if (_conditions != null) {
                _conditionsMet = _conditions.canBeMetBy(_attemptingUnit.getAttemptingPersonnel(cardGame), cardGame);
            } else {
                _conditionsMet = true;
            }
        } else {
            if (_conditionsMet) {
                cardGame.addActionToStack(new RemoveDilemmaFromGameAction(cardGame, _performingPlayerId, _dilemma));
                setAsSuccessful();
            } else {
                if (!_additionalFailActions.isEmpty()) {
                    Action failAction = _additionalFailActions.getFirst();
                    _additionalFailActions.remove(failAction);
                    cardGame.addActionToStack(failAction);
                } else if (!_cardsStopped) {
                    Collection<ST1EPhysicalCard> cardsToStop =
                            new LinkedList<>(_attemptingUnit.getAttemptingPersonnel(cardGame));
                    if (_attemptingUnit instanceof ShipCard ship) {
                        cardsToStop.add(ship);
                    }
                    _cardsStopped = true;
                    cardGame.addActionToStack(new StopCardsAction(cardGame, _performingPlayerId, cardsToStop));
                } else if (_discardDilemma && !_dilemmaDiscarded) {
                    _dilemmaDiscarded = true;
                    cardGame.addActionToStack(new RemoveDilemmaFromGameAction(cardGame, _performingPlayerId, _dilemma));
                } else {
                    _encounterAction.setAsFailed();
                    _encounterAction.getAttemptAction().setAsFailed();
                    setAsFailed();
                }
            }
        }
    }
}