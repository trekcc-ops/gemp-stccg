package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.CardPerformedAction;
import com.gempukku.stccg.actions.discard.RemoveDilemmaFromGameAction;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.actions.missionattempt.FailDilemmaAction;
import com.gempukku.stccg.actions.missionattempt.OvercomeDilemmaConditionAction;
import com.gempukku.stccg.actions.modifiers.KillSinglePersonnelAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.condition.missionrequirements.MissionRequirement;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.PlayerNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class OvercomeDilemmaConditionActionBlueprint extends DelayedEffectBlueprint {

    private final MissionRequirement _conditions;

    public OvercomeDilemmaConditionActionBlueprint(@JsonProperty(value = "requires", required = true)
            MissionRequirement conditions) {
        _conditions = conditions;
    }

    @Override
    protected List<Action> createActions(CardPerformedAction action, ActionContext context)
            throws InvalidGameLogicException, InvalidCardDefinitionException, PlayerNotFoundException {
        List<Action> result = new ArrayList<>();
        DefaultGame cardGame = context.getGame();
        Stack<Action> actionStack = cardGame.getActionsEnvironment().getActionStack();
        for (Action pendingAction : actionStack) {
            if (pendingAction instanceof EncounterSeedCardAction encounterAction &&
                    encounterAction.getEncounteredCard() == context.getSource()) {
                result.add(new OvercomeDilemmaConditionAction(context.getSource(), encounterAction, _conditions,
                        encounterAction.getAttemptingUnit()));
            }
        }
        return result;
    }
}