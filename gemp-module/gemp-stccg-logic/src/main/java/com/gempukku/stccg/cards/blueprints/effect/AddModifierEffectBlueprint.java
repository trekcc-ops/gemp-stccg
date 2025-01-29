package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.CardPerformedAction;
import com.gempukku.stccg.actions.modifiers.AddUntilModifierAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.blueprints.requirement.Requirement;
import com.gempukku.stccg.cards.blueprints.requirement.RequirementFactory;
import com.gempukku.stccg.cards.blueprints.resolver.TimeResolver;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.condition.RequirementCondition;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.modifiers.CantDiscardFromPlayByPlayerModifier;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.attributes.StrengthModifier;
import com.gempukku.stccg.modifiers.blueprints.ModifierBlueprint;

import java.util.List;

public class AddModifierEffectBlueprint extends DelayedEffectBlueprint {

    private final TimeResolver.Time _until;
    private final ModifierBlueprint _modifierSource;

    public AddModifierEffectBlueprint(JsonNode node) throws InvalidCardDefinitionException {
        BlueprintUtils.validateAllowedFields(node, "modifier", "until");
        _until = TimeResolver.resolveTime(node.get("until"), "end(current)");
        _modifierSource = getModifier(node.get("modifier"));
    }

    public static ModifierBlueprint getModifier(JsonNode node) throws InvalidCardDefinitionException {
        ModifierSourceProcessorType modifierType =
                BlueprintUtils.getEnum(ModifierSourceProcessorType.class, node, "type");
        validateAllowedFields(node, modifierType);

        final List<Requirement> requirements = List.of(RequirementFactory.getRequirements(node));
        final FilterableSource filterableSource;

        switch(modifierType) {
            case MODIFYSTRENGTH:
                ValueSource valueSource = ValueResolver.resolveEvaluator(node.get("amount").textValue());
                filterableSource = BlueprintUtils.getFilterable(node);
                return (actionContext) -> {
                    final Evaluator evaluator = valueSource.getEvaluator(actionContext);
                    return new StrengthModifier(actionContext,
                            filterableSource.getFilterable(actionContext),
                            new RequirementCondition(requirements, actionContext), evaluator);
                };
            case OPPONENTMAYNOTDISCARD:
                filterableSource = BlueprintUtils.getFilterable(node);
                return (actionContext) -> new CantDiscardFromPlayByPlayerModifier(
                        actionContext.getSource(), "Can't be discarded by opponent",
                        filterableSource.getFilterable(actionContext), actionContext.getPerformingPlayerId());
            default:
                throw new InvalidCardDefinitionException("Unable to resolve modifier of type: " + modifierType);
        }
    }

    private static void validateAllowedFields(JsonNode node, ModifierSourceProcessorType modifierType)
            throws InvalidCardDefinitionException {
        switch(modifierType) {
            case CANTPLAYCARDS:
                BlueprintUtils.validateAllowedFields(node, "filter", "requires");
                break;
            case GAINICON:
                BlueprintUtils.validateAllowedFields(node, "filter", "requires", "icon");
                break;
            case MODIFYSTRENGTH:
                BlueprintUtils.validateAllowedFields(node, "filter", "requires", "amount");
                break;
            case OPPONENTMAYNOTDISCARD:
                BlueprintUtils.validateAllowedFields(node, "filter");
                break;
            default:
                throw new InvalidCardDefinitionException("Unable to resolve modifier of type : " + modifierType);
        }
    }

    @Override
    protected List<Action> createActions(CardPerformedAction parentAction, ActionContext context) {
        final Modifier modifier = _modifierSource.getModifier(context);
        Action action = new AddUntilModifierAction(context.getSource(), modifier, _until);
        return List.of(action);
    }

    private enum ModifierSourceProcessorType { CANTPLAYCARDS, GAINICON, MODIFYSTRENGTH, OPPONENTMAYNOTDISCARD }
}