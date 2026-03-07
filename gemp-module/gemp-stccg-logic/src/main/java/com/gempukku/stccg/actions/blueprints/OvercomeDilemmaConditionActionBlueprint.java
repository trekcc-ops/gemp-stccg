package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.missionattempt.AttemptMissionAction;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.actions.missionattempt.OvercomeDilemmaConditionAction;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.condition.missionrequirements.MissionRequirement;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

import java.util.*;

public class OvercomeDilemmaConditionActionBlueprint implements SubActionBlueprint {

    private final MissionRequirement _conditions;
    private final boolean _discardDilemma;
    private final List<SubActionBlueprint> _failEffects;
    private final SubActionBlueprint _costAction;
    private final List<SubActionBlueprint> _successEffects = new ArrayList<>();

    private OvercomeDilemmaConditionActionBlueprint(@JsonProperty(value = "requires")
            MissionRequirement conditions,
                                                   @JsonProperty("discardDilemma")
                                                   boolean discardDilemma,
                                                   @JsonProperty("costAction") SubActionBlueprint costAction,
                                                   @JsonProperty("failEffect")
                                                   @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                                                   List<SubActionBlueprint> failEffects,
                                                    @JsonProperty("successAction")
                                                    SubActionBlueprint successAction)
            throws InvalidCardDefinitionException {
        _conditions = conditions;
        _discardDilemma = discardDilemma;
        _failEffects = Objects.requireNonNullElse(failEffects, new ArrayList<>());
        _costAction = costAction;
        if (successAction != null) {
            _successEffects.add(successAction);
        }
        if (conditions == null && costAction == null) {
            throw new InvalidCardDefinitionException("Cannot create an overcome dilemma action with no requirement or cost");
        }
    }

    public OvercomeDilemmaConditionAction createAction(DefaultGame cardGame, GameTextContext context) {
        Stack<Action> actionStack = cardGame.getActionsEnvironment().getActionStack();
        OvercomeDilemmaConditionAction actionToReturn = null;
        for (Action pendingAction : actionStack) {
            if (pendingAction instanceof EncounterSeedCardAction encounterAction &&
                    encounterAction.getEncounteredCard() == context.card()) {
                actionToReturn = new OvercomeDilemmaConditionAction(cardGame, context.card(),
                        encounterAction, _conditions, encounterAction.getAttemptingUnit(), _failEffects,
                        _successEffects, _discardDilemma, context);
                break;
            } else if (pendingAction instanceof AttemptMissionAction attemptAction &&
                    !context.card().isBeingEncountered(cardGame)
            ) {
                try {
                    actionToReturn = new OvercomeDilemmaConditionAction(cardGame, context.card(),
                            attemptAction, _conditions, attemptAction.getAttemptingUnit(), _failEffects,
                            _successEffects, _discardDilemma, context);
                    break;
                } catch(InvalidGameLogicException ignored) {

                }
            }
        }
        if (actionToReturn != null && _costAction != null) {
            actionToReturn.appendCost(_costAction);
        }
        return actionToReturn;
    }

    @Override
    public Collection<ActionBlueprint> getAllTheoreticalSubActions() {
        Collection<ActionBlueprint> result = new ArrayList<>();
        if (_costAction != null) {
            result.add(_costAction);
        }
        if (!_failEffects.isEmpty()) {
            result.addAll(_failEffects);
        }
        if (!_successEffects.isEmpty()) {
            result.addAll(_successEffects);
        }
        return result;
    }
}