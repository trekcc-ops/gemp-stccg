package com.gempukku.stccg.actions.missionattempt;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.discard.RemoveDilemmaFromGameAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.condition.missionrequirements.MissionRequirement;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

public class OvercomeDilemmaConditionAction extends ActionyAction {

    private final AttemptingUnit _attemptingUnit;
    private final Action _failAction;
    private final int _succeedActionId;
    private final EncounterSeedCardAction _encounterAction;
    private enum Progress { conditionsChecked }
    private final MissionRequirement _conditions;
    private boolean _failed;
    private boolean _succeeded;

    public OvercomeDilemmaConditionAction(DefaultGame cardGame, PhysicalCard dilemma,
                                          EncounterSeedCardAction encounterAction, MissionRequirement conditions,
                                          AttemptingUnit attemptingUnit, Action failDilemmaAction) {
        super(cardGame, attemptingUnit.getControllerName(), ActionType.OVERCOME_DILEMMA, Progress.values());
        _attemptingUnit = attemptingUnit;
        _failAction = new FailDilemmaAction(cardGame, attemptingUnit, dilemma, failDilemmaAction, encounterAction);
        Action succeedAction = new RemoveDilemmaFromGameAction(cardGame, attemptingUnit.getControllerName(), dilemma);
        _succeedActionId = succeedAction.getActionId();
        _conditions = conditions;
        _encounterAction = encounterAction;
    }

    public OvercomeDilemmaConditionAction(DefaultGame cardGame, PhysicalCard dilemma, EncounterSeedCardAction action,
                                          MissionRequirement conditions, AttemptingUnit attemptingUnit) {
        super(cardGame, attemptingUnit.getControllerName(), ActionType.OVERCOME_DILEMMA, Progress.values());
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
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        if (isBeingInitiated())
            setAsInitiated();

        if (!getProgress(Progress.conditionsChecked)) {
            Action result;
            if (_conditions.canBeMetBy(_attemptingUnit.getAttemptingPersonnel(cardGame), cardGame)) {
                result = cardGame.getActionById(_succeedActionId);
                _succeeded = true;
            } else {
                result = _failAction;
                _encounterAction.setAsFailed();
                _encounterAction.getAttemptAction().setAsFailed();
                _failed = true;
            }
            setProgress(Progress.conditionsChecked);
            return result;
        }

        Action nextAction = getNextAction();
        if (nextAction == null) {
            if (_succeeded)
                setAsSuccessful();
            else setAsFailed();
        }
        return nextAction;
    }
}