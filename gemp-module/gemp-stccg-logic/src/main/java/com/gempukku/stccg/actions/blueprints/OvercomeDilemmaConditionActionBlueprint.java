package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.actions.missionattempt.OvercomeDilemmaConditionAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.condition.missionrequirements.MissionRequirement;
import com.gempukku.stccg.game.DefaultGame;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class OvercomeDilemmaConditionActionBlueprint implements SubActionBlueprint {

    private final MissionRequirement _conditions;
    private final boolean _discardDilemma;
    private final List<SubActionBlueprint> _failEffects;

    public OvercomeDilemmaConditionActionBlueprint(@JsonProperty(value = "requires", required = true)
            MissionRequirement conditions,
                                                   @JsonProperty("discardDilemma")
                                                   boolean discardDilemma,
                                                   @JsonProperty("failEffect")
                                                           SubActionBlueprint failEffect) {
        _conditions = conditions;
        _discardDilemma = discardDilemma;
        _failEffects = (failEffect == null) ? new LinkedList<>() : List.of(failEffect);
    }

    @Override
    public List<Action> createActions(DefaultGame cardGame, ActionWithSubActions action, ActionContext context) {
        List<Action> result = new ArrayList<>();
        Stack<Action> actionStack = cardGame.getActionsEnvironment().getActionStack();
        for (Action pendingAction : actionStack) {
            if (pendingAction instanceof EncounterSeedCardAction encounterAction &&
                    encounterAction.getEncounteredCard() == context.card()) {
                result.add(new OvercomeDilemmaConditionAction(cardGame, context.card(),
                        encounterAction, _conditions, encounterAction.getAttemptingUnit()));
            }
        }
        return result;
    }
}