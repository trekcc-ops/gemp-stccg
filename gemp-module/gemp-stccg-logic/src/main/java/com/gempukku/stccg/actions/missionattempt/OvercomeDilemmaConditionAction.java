package com.gempukku.stccg.actions.missionattempt;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.blueprints.SubActionBlueprint;
import com.gempukku.stccg.actions.discard.RemoveDilemmaFromGameAction;
import com.gempukku.stccg.actions.modifiers.StopCardsAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.cards.physicalcard.StoppableCard;
import com.gempukku.stccg.condition.missionrequirements.MissionRequirement;
import com.gempukku.stccg.game.DefaultGame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class OvercomeDilemmaConditionAction extends ActionyAction {

    private final AttemptingUnit _attemptingUnit;
    private final ActionWithSubActions _parentAction; // typically an encounter, but sometimes an attempt
    private final MissionRequirement _conditions;
    private boolean _conditionsChecked;
    private boolean _conditionsMet;
    private final boolean _discardDilemma;
    private final PhysicalCard _dilemma;
    private final List<SubActionBlueprint> _additionalFailActions = new ArrayList<>();
    private final List<SubActionBlueprint> _additionalSuccessActions = new ArrayList<>();


    public OvercomeDilemmaConditionAction(DefaultGame cardGame, PhysicalCard dilemma,
                                          ActionWithSubActions parentAction, MissionRequirement conditions,
                                          AttemptingUnit attemptingUnit, List<SubActionBlueprint> failDilemmaActions,
                                          List<SubActionBlueprint> successActions, boolean discardDilemma,
                                          GameTextContext context) {
        super(cardGame, attemptingUnit.getControllerName(), ActionType.OVERCOME_DILEMMA, context);
        _dilemma = dilemma;
        _attemptingUnit = attemptingUnit;
        _additionalFailActions.addAll(failDilemmaActions);
        _conditions = conditions;
        _parentAction = parentAction;
        _discardDilemma = discardDilemma;
        _additionalSuccessActions.addAll(successActions);
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
                _parentAction.insertSubActions(_additionalSuccessActions);
                setAsSuccessful();
            } else {
                setAsFailed();
            }
        }
    }

    @Override
    public void setAsFailed() {
        _parentAction.removeRemainingSubActions();
        _parentAction.insertSubActions(_additionalFailActions);
        _parentAction.appendSubAction(new SubActionBlueprint() {
            @Override
            public Action createAction(DefaultGame cardGame, ActionWithSubActions parentAction, GameTextContext context) {
                Collection<StoppableCard> cardsToStop =
                        new LinkedList<>(_attemptingUnit.getAttemptingPersonnel(cardGame));
                if (_attemptingUnit instanceof ShipCard ship) {
                    cardsToStop.add(ship);
                }
                return new StopCardsAction(cardGame, _performingPlayerId, cardsToStop);
            }
        });
        _parentAction.appendSubAction(new SubActionBlueprint() {
            @Override
            public Action createAction(DefaultGame cardGame, ActionWithSubActions parentAction, GameTextContext context) {
                if (_discardDilemma && _dilemma.getParentCard() == null) {
                    return new RemoveDilemmaFromGameAction(cardGame, _performingPlayerId, _dilemma);
                } else {
                    return null;
                }
            }
        });

        if (_parentAction instanceof AttemptMissionAction attemptAction) {
            attemptAction.setAsConditionFailed();
        }
        super.setAsFailed();
    }
}