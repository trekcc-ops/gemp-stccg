package com.gempukku.stccg.cards.blueprints.fieldprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.actionsource.*;
import com.gempukku.stccg.common.JsonUtils;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.TriggerTiming;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.trigger.TriggerChecker;

import java.util.*;

public class EffectFieldProcessor implements FieldProcessor {

    @Override
    public void processField(String key, JsonNode value, CardBlueprint blueprint, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        List<JsonNode> effectList = JsonUtils.toArray(value);
        for (JsonNode effect : effectList) {
            final String effectType = effect.get("type").textValue().toLowerCase();
            switch (effectType) {
                case "action":
                    appendActionSource(effect, blueprint, environment);
                    break;
                case "modifier":
                    environment.validateAllowedFields(effect, "modifier");
                    blueprint.appendInPlayModifier(environment.getModifier(effect.get("modifier")));
                    break;
                case "playoutofsequence":
                    environment.validateAllowedFields(effect, "requires");
                    final Requirement[] conditions = environment.getRequirementsFromJSON(effect);
                    blueprint.appendPlayOutOfSequenceCondition(actionContext -> actionContext.acceptsAllRequirements(conditions));
                    break;
                case "seed":
                    appendSeedActionSource(effect, blueprint, environment);
                    break;
                default:
                    throw new InvalidCardDefinitionException("Unable to find effect of type: " + effectType);
            }
        }
    }

    private void appendActionSource(JsonNode node, CardBlueprint blueprint, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node,
                "text", "optional", "limitPerPhase", "limitPerTurn", "phase", "trigger",
                "requires", "cost", "effect");

        List<DefaultActionSource> actionSourceList = new LinkedList<>();
        boolean isResponse = node.has("trigger");

        String text = environment.getString(node, "text");
        final boolean optional = environment.getBoolean(node, "optional", false);
        final int limitPerPhase = environment.getInteger(node, "limitPerPhase", 0);
        final int limitPerTurn = environment.getInteger(node, "limitPerTurn", 0);
        final Phase phase = environment.getEnum(Phase.class, environment.getString(node, "phase"), "phase");

        if (!isResponse) {
            actionSourceList.add(new ActivateCardActionSource());
        } else {
            List<JsonNode> triggers = JsonUtils.toArray(node.get("trigger"));
            for (JsonNode trigger : triggers) {
                TriggerChecker triggerChecker =
                        environment.getTriggerCheckerFactory().getTriggerChecker(trigger, environment);
                TriggerTiming triggerTiming = triggerChecker.isBefore() ? TriggerTiming.BEFORE : TriggerTiming.AFTER;
                TriggerActionSource triggerActionSource;
                if (optional) {
                    triggerActionSource = new OptionalTriggerActionSource(triggerTiming);
                } else {
                    triggerActionSource = new RequiredTriggerActionSource(triggerTiming);
                }
                triggerActionSource.addRequirement(triggerChecker);
                actionSourceList.add(triggerActionSource);
            }
        }

        for (DefaultActionSource actionSource : actionSourceList) {
            if (text != null)
                actionSource.setText(text);
            if (limitPerPhase > 0)
                actionSource.setPhaseLimit(phase, limitPerPhase);
            if (limitPerTurn > 0)
                actionSource.setTurnLimit(limitPerTurn);
            if (phase != null)
                actionSource.addRequirement(
                        (actionContext) -> actionContext.getGameState().getCurrentPhase() == phase);
            actionSource.processRequirementsCostsAndEffects(node, environment);

            if (!isResponse)
                blueprint.appendInPlayPhaseAction(actionSource);
            else if (actionSource instanceof TriggerActionSource triggerSource)
                blueprint.appendBeforeOrAfterTrigger(triggerSource);
            else throw new InvalidCardDefinitionException("Something went wrong while adding action source");
        }
    }

    private void appendSeedActionSource(JsonNode value, CardBlueprint blueprint, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(value, "limit", "where");
        SeedCardActionSource actionSource = new SeedCardActionSource();
        if (value.has("limit"))
            actionSource.addRequirement((actionContext) -> actionContext.getSource()
                    .getNumberOfCopiesSeededByPlayer(actionContext.getPerformingPlayer()) < value.get("limit").asInt());
        if (value.has("where")) {
            if (Objects.equals(value.get("where").textValue(), "table"))
                actionSource.setSeedZone(Zone.TABLE);
            else throw new InvalidCardDefinitionException("Unknown parameter in seed:where field");
        }
        blueprint.setSeedCardActionSource(actionSource);
    }
}