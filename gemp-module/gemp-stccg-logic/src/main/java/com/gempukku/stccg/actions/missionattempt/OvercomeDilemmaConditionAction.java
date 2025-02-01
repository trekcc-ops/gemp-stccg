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
    private final int _failActionId;
    private final int _succeedActionId;
    private final EncounterSeedCardAction _encounterAction;
    private enum Progress { conditionsChecked }
    private final MissionRequirement _conditions;

    public OvercomeDilemmaConditionAction(PhysicalCard dilemma, EncounterSeedCardAction encounterAction,
                                          MissionRequirement conditions, AttemptingUnit attemptingUnit,
                                          Action failDilemmaAction) {
        super(dilemma.getGame(), attemptingUnit.getPlayer(), ActionType.OVERCOME_DILEMMA, Progress.values());
        _attemptingUnit = attemptingUnit;
        Action failAction =
                new FailDilemmaAction(dilemma.getGame(), attemptingUnit, dilemma, failDilemmaAction, encounterAction);
        _failActionId = failAction.getActionId();
        Action succeedAction = new RemoveDilemmaFromGameAction(attemptingUnit.getPlayer(), dilemma
        );
        _succeedActionId = succeedAction.getActionId();
        _conditions = conditions;
        _encounterAction = encounterAction;
    }

    public OvercomeDilemmaConditionAction(PhysicalCard dilemma, EncounterSeedCardAction action,
                                          MissionRequirement conditions, AttemptingUnit attemptingUnit) {
        super(dilemma.getGame(), attemptingUnit.getPlayer(), ActionType.OVERCOME_DILEMMA, Progress.values());
        _attemptingUnit = attemptingUnit;
        Action failAction = new FailDilemmaAction(attemptingUnit, dilemma, action);
        _failActionId = failAction.getActionId();
        Action succeedAction = new RemoveDilemmaFromGameAction(attemptingUnit.getPlayer(), dilemma
        );
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
            if (_conditions.canBeMetBy(_attemptingUnit)) {
                result = cardGame.getActionById(_succeedActionId);
                setAsSuccessful();
            } else {
                result = cardGame.getActionById(_failActionId);
                _encounterAction.setAsFailed();
                _encounterAction.getAttemptAction().setAsFailed();
                setAsFailed();
            }
            setProgress(Progress.conditionsChecked);
            return result;
        }

        return getNextAction();
    }
}