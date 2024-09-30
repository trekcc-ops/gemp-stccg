package com.gempukku.stccg.cards.blueprints.modifiersourceproducer;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.ModifierSource;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.modifiers.CantDiscardFromPlayByPlayerModifier;
import com.gempukku.stccg.modifiers.CantPlayCardsModifier;
import com.gempukku.stccg.modifiers.GainIconModifier;
import com.gempukku.stccg.modifiers.RequirementCondition;
import com.gempukku.stccg.modifiers.attributes.StrengthModifier;
import com.gempukku.stccg.requirement.Requirement;

public class ModifierSourceFactory {

    private enum ModifierSourceProcessorType { CANTPLAYCARDS, GAINICON, MODIFYSTRENGTH, OPPONENTMAYNOTDISCARD }

    private void validateAllowedFields(JsonNode node, ModifierSourceProcessorType modifierType,
                                       CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        switch(modifierType) {
            case CANTPLAYCARDS:
                environment.validateAllowedFields(node, "filter", "requires");
                break;
            case GAINICON:
                environment.validateAllowedFields(node, "filter", "requires", "icon");
                break;
            case MODIFYSTRENGTH:
                environment.validateAllowedFields(node, "filter", "requires", "amount");
                break;
            case OPPONENTMAYNOTDISCARD:
                environment.validateAllowedFields(node, "filter");
                break;
            default:
                throw new InvalidCardDefinitionException("Unable to resolve modifier of type : " + modifierType);
        }
    }
    
    public ModifierSource getModifierSource(JsonNode node, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        ModifierSourceProcessorType modifierType = environment.getEnum(
                ModifierSourceProcessorType.class, node, "type");
        validateAllowedFields(node, modifierType, environment);

        final Requirement[] requirements = environment.getRequirementsFromJSON(node);
        final FilterableSource filterableSource;

        switch(modifierType) {
            case CANTPLAYCARDS:
                filterableSource = environment.getFilterable(node);
                return (actionContext) -> new CantPlayCardsModifier(actionContext.getSource(),
                        new RequirementCondition(requirements, actionContext),
                        filterableSource.getFilterable(actionContext));
            case GAINICON:
                CardIcon icon = environment.getEnum(CardIcon.class, node, "icon");
                filterableSource = environment.getFilterFactory().parseSTCCGFilter(node.get("filter").textValue());
                return actionContext -> new GainIconModifier(actionContext,
                        filterableSource.getFilterable(actionContext),
                        new RequirementCondition(requirements, actionContext), icon);
            case MODIFYSTRENGTH:
                ValueSource valueSource = ValueResolver.resolveEvaluator(node.get("amount"), environment);
                filterableSource = environment.getFilterable(node);
                return (actionContext) -> {
                    final Evaluator evaluator = valueSource.getEvaluator(actionContext);
                    return new StrengthModifier(actionContext,
                            filterableSource.getFilterable(actionContext),
                            new RequirementCondition(requirements, actionContext), evaluator);
                };
            case OPPONENTMAYNOTDISCARD:
                filterableSource = environment.getFilterable(node);
                return (actionContext) -> new CantDiscardFromPlayByPlayerModifier(
                        actionContext.getSource(), "Can't be discarded by opponent",
                        filterableSource.getFilterable(actionContext), actionContext.getPerformingPlayerId());
            default:
                throw new InvalidCardDefinitionException("Unable to resolve modifier of type: " + modifierType);
        }
    }
}