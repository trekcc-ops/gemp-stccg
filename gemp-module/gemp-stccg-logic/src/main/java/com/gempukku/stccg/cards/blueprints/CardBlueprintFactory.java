package com.gempukku.stccg.cards.blueprints;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.effect.EffectBlueprint;
import com.gempukku.stccg.cards.blueprints.effect.EffectBlueprintDeserializer;
import com.gempukku.stccg.cards.blueprints.modifiersourceproducer.ModifierSource;
import com.gempukku.stccg.cards.blueprints.requirement.Requirement;
import com.gempukku.stccg.cards.blueprints.requirement.RequirementFactory;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.cards.blueprints.trigger.TriggerCheckerFactory;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.modifiers.CantDiscardFromPlayByPlayerModifier;
import com.gempukku.stccg.modifiers.CantPlayCardsModifier;
import com.gempukku.stccg.modifiers.GainIconModifier;
import com.gempukku.stccg.modifiers.RequirementCondition;
import com.gempukku.stccg.modifiers.attributes.StrengthModifier;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class CardBlueprintFactory {
    private final EffectBlueprintDeserializer effectAppenderFactory = new EffectBlueprintDeserializer(this);
    private final FilterFactory filterFactory = new FilterFactory(this);
    private final TriggerCheckerFactory triggerCheckerFactory = new TriggerCheckerFactory();

    public EffectBlueprintDeserializer getEffectAppenderFactory() {
        return effectAppenderFactory;
    }

    public FilterFactory getFilterFactory() {
        return filterFactory;
    }

    public TriggerCheckerFactory getTriggerCheckerFactory() {
        return triggerCheckerFactory;
    }

    public FilterableSource getCardFilterableIfChooseOrAll(String filter) throws InvalidCardDefinitionException {
        return (filter.startsWith("all(") || filter.startsWith("choose(")) ?
                getFilterFactory().generateFilter(filter.substring(filter.indexOf("(") + 1, filter.lastIndexOf(")"))) :
                null;
    }


    public JsonNode[] getNodeArray(JsonNode node) {
        List<JsonNode> nodes = new LinkedList<>();
        if (node == null)
            return new JsonNode[0];
        else if (node.isArray()) {
            for (JsonNode elem : node) {
                nodes.add(elem);
            }
        } else {
            nodes.add(node);
        }
        return nodes.toArray(new JsonNode[0]);
    }

    public String[] getStringArray(JsonNode node) {
        JsonNode[] nodeArray = getNodeArray(node);
        String[] stringArray = new String[nodeArray.length];
        for (int i = 0; i < nodeArray.length; i++) {
            stringArray[i] = nodeArray[i].textValue();
        }
        return stringArray;
    }


    public Requirement[] getRequirementsFromJSON(JsonNode parentNode) throws InvalidCardDefinitionException {
        RequirementFactory factory = new RequirementFactory();
        List<Requirement> result = new LinkedList<>();
        if (parentNode.has("requires")) {
            if (parentNode.get("requires").isArray()) {
                for (JsonNode requirement : parentNode.get("requires"))
                    result.add(factory.getRequirement(requirement));
            } else result.add(factory.getRequirement(parentNode.get("requires")));
        }
        return result.toArray(new Requirement[0]);
    }


    public List<EffectBlueprint> getEffectAppendersFromJSON(JsonNode node)
            throws InvalidCardDefinitionException {
        List<EffectBlueprint> appenders = new LinkedList<>();
        if (node.isArray()) {
            for (JsonNode effect : node)
                appenders.add(effectAppenderFactory.getEffectAppender(effect));
        } else {
            appenders.add(effectAppenderFactory.getEffectAppender(node));
        }
        return appenders;
    }


    public ModifierSource getModifier(JsonNode node) throws InvalidCardDefinitionException {
        ModifierSourceProcessorType modifierType = BlueprintUtils.getEnum(ModifierSourceProcessorType.class, node, "type");
        validateAllowedFields(node, modifierType, this);

        final Requirement[] requirements = getRequirementsFromJSON(node);
        final FilterableSource filterableSource;

        switch(modifierType) {
            case CANTPLAYCARDS:
                filterableSource = BlueprintUtils.getFilterable(node);
                return (actionContext) -> new CantPlayCardsModifier(actionContext.getSource(),
                        new RequirementCondition(requirements, actionContext),
                        filterableSource.getFilterable(actionContext));
            case GAINICON:
                CardIcon icon = BlueprintUtils.getEnum(CardIcon.class, node, "icon");
                filterableSource = getFilterFactory().parseSTCCGFilter(node.get("filter").textValue());
                return actionContext -> new GainIconModifier(actionContext,
                        filterableSource.getFilterable(actionContext),
                        new RequirementCondition(requirements, actionContext), icon);
            case MODIFYSTRENGTH:
                ValueSource valueSource = ValueResolver.resolveEvaluator(node.get("amount"));
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

    private enum ModifierSourceProcessorType { CANTPLAYCARDS, GAINICON, MODIFYSTRENGTH, OPPONENTMAYNOTDISCARD }
    
    private void validateAllowedFields(JsonNode node, ModifierSourceProcessorType modifierType,
                                       CardBlueprintFactory environment) throws InvalidCardDefinitionException {
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


    public Function<ActionContext, List<PhysicalCard>> getCardSourceFromZone(PlayerSource player, Zone zone,
                                                                                    String filter)
            throws InvalidCardDefinitionException {
        String sourceMemory = (filter.startsWith("memory(")) ?
                filter.substring(filter.indexOf("(") + 1, filter.lastIndexOf(")")) : null;
        return switch (zone) {
            case HAND, DISCARD, DRAW_DECK -> actionContext -> Filters.filter(
                    actionContext.getGameState().getZoneCards(player.getPlayerId(actionContext), zone),
                    sourceMemory == null ?
                            Filters.any : Filters.in(actionContext.getCardsFromMemory(sourceMemory))).stream().toList();
            default -> throw new InvalidCardDefinitionException(
                    "getCardSource function not defined for zone " + zone.getHumanReadable());
        };
    }

}