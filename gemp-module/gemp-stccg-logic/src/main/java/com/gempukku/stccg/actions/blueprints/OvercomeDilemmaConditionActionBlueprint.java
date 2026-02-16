package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.actions.missionattempt.OvercomeDilemmaConditionAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.condition.missionrequirements.MissionRequirement;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

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

    @Override
    public List<Action> createActions(DefaultGame cardGame, ActionWithSubActions action, ActionContext context) {
        List<Action> result = new ArrayList<>();
        Stack<Action> actionStack = cardGame.getActionsEnvironment().getActionStack();
        OvercomeDilemmaConditionAction actionToReturn = null;
        for (Action pendingAction : actionStack) {
            if (pendingAction instanceof EncounterSeedCardAction encounterAction &&
                    encounterAction.getEncounteredCard() == context.card()) {
                actionToReturn = new OvercomeDilemmaConditionAction(cardGame, context.card(),
                        encounterAction, _conditions, encounterAction.getAttemptingUnit(), _failEffects,
                        _successEffects, _discardDilemma, context);
                result.add(actionToReturn);
                break;
            }
        }
        if (actionToReturn != null && _costAction != null) {
            try {
                List<Action> costActions = _costAction.createActions(cardGame, action, context);
                for (Action costAction : costActions) {
                    actionToReturn.appendCost(costAction);
                }
            } catch(PlayerNotFoundException | InvalidCardDefinitionException | InvalidGameLogicException ignored) {

            }
        }
        return result;
    }
}