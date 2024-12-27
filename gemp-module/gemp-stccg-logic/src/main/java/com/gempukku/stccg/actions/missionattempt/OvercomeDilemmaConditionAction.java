package com.gempukku.stccg.actions.missionattempt;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.discard.RemoveDilemmaFromGameAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.condition.missionrequirements.MissionRequirement;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;

import java.util.Collection;
import java.util.LinkedList;

public class OvercomeDilemmaConditionAction extends ActionyAction {

    private final AttemptingUnit _attemptingUnit;
    private final PhysicalCard _dilemma;
    private final int _failActionId;
    private final int _succeedActionId;
    private enum Progress { conditionsChecked }
    private final MissionRequirement _conditions;

    public OvercomeDilemmaConditionAction(PhysicalCard dilemma, MissionRequirement conditions,
                                          AttemptingUnit attemptingUnit, Action failDilemmaAction,
                                          EncounterSeedCardAction encounterAction) {
        super(attemptingUnit.getPlayer(), ActionType.OVERCOME_DILEMMA, Progress.values());
        _attemptingUnit = attemptingUnit;
        _dilemma = dilemma;
        Action failAction = new FailDilemmaAction(attemptingUnit, dilemma, encounterAction);
        failAction.insertEffect(failDilemmaAction);
        _failActionId = failAction.getActionId();
        Action succeedAction = new RemoveDilemmaFromGameAction(attemptingUnit.getPlayer(), dilemma,
                encounterAction.getLocation());
        _succeedActionId = succeedAction.getActionId();
        _conditions = conditions;
    }

    @Override
    public PhysicalCard getPerformingCard() {
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
        if (!getProgress(Progress.conditionsChecked)) {
            Action result;
            if (_conditions.canBeMetBy(_attemptingUnit)) {
                result = cardGame.getActionById(_succeedActionId);
            } else {
                result = cardGame.getActionById(_failActionId);
            }
            setProgress(Progress.conditionsChecked);
            return result;
        }

        return getNextAction();
    }
}