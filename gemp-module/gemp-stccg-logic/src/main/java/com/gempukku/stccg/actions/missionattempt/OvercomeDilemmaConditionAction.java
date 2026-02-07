package com.gempukku.stccg.actions.missionattempt;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.discard.RemoveDilemmaFromGameAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.condition.missionrequirements.MissionRequirement;
import com.gempukku.stccg.game.DefaultGame;

public class OvercomeDilemmaConditionAction extends ActionyAction {

    private final AttemptingUnit _attemptingUnit;
    private final Action _failAction;
    private final int _succeedActionId;
    private final EncounterSeedCardAction _encounterAction;
    private final MissionRequirement _conditions;

    public OvercomeDilemmaConditionAction(DefaultGame cardGame, PhysicalCard dilemma,
                                          EncounterSeedCardAction encounterAction, MissionRequirement conditions,
                                          AttemptingUnit attemptingUnit, Action failDilemmaAction) {
        super(cardGame, attemptingUnit.getControllerName(), ActionType.OVERCOME_DILEMMA);
        _attemptingUnit = attemptingUnit;
        _failAction = new FailDilemmaAction(cardGame, attemptingUnit, dilemma, failDilemmaAction, encounterAction);
        Action succeedAction = new RemoveDilemmaFromGameAction(cardGame, attemptingUnit.getControllerName(), dilemma);
        _succeedActionId = succeedAction.getActionId();
        _conditions = conditions;
        _encounterAction = encounterAction;
    }

    public OvercomeDilemmaConditionAction(DefaultGame cardGame, PhysicalCard dilemma, EncounterSeedCardAction action,
                                          MissionRequirement conditions, AttemptingUnit attemptingUnit) {
        super(cardGame, attemptingUnit.getControllerName(), ActionType.OVERCOME_DILEMMA);
        _attemptingUnit = attemptingUnit;
        _failAction = new FailDilemmaAction(cardGame, attemptingUnit, dilemma, action);
        Action succeedAction = new RemoveDilemmaFromGameAction(cardGame, attemptingUnit.getControllerName(), dilemma);
        _succeedActionId = succeedAction.getActionId();
        _conditions = conditions;
        _encounterAction = action;
    }


    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    protected void processEffect(DefaultGame cardGame) {
        Action result;
        if (_conditions.canBeMetBy(_attemptingUnit.getAttemptingPersonnel(cardGame), cardGame)) {
            result = cardGame.getActionById(_succeedActionId);
            setAsSuccessful();
        } else {
            setAsFailed();
            result = _failAction;
            _encounterAction.setAsFailed();
            _encounterAction.getAttemptAction().setAsFailed();
        }
        cardGame.addActionToStack(result);
    }
}