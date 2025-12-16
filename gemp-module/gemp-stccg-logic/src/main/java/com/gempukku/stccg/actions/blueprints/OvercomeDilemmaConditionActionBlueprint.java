package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.actions.missionattempt.OvercomeDilemmaConditionAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.condition.missionrequirements.MissionRequirement;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.*;

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
    public List<Action> createActions(DefaultGame cardGame, ActionWithSubActions action, ActionContext context)
            throws InvalidGameLogicException, InvalidCardDefinitionException, PlayerNotFoundException {
        List<Action> result = new ArrayList<>();
        Stack<Action> actionStack = cardGame.getActionsEnvironment().getActionStack();
        for (Action pendingAction : actionStack) {
            if (pendingAction instanceof EncounterSeedCardAction encounterAction &&
                    encounterAction.getEncounteredCard() == context.getPerformingCard(cardGame)) {
                result.add(new OvercomeDilemmaConditionAction(cardGame, context.getPerformingCard(cardGame),
                        encounterAction, _conditions, encounterAction.getAttemptingUnit()));
            }
        }
        return result;
    }
}