package com.gempukku.stccg.cards.blueprints;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.effect.ModifierSource;
import com.gempukku.stccg.cards.blueprints.requirement.Requirement;
import com.gempukku.stccg.cards.blueprints.requirement.RequirementFactory;
import com.gempukku.stccg.cards.blueprints.resolver.PlayerResolver;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardIcon;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.condition.RequirementCondition;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.PlayerNotFoundException;
import com.gempukku.stccg.modifiers.CantDiscardFromPlayByPlayerModifier;
import com.gempukku.stccg.modifiers.GainIconModifier;
import com.gempukku.stccg.modifiers.attributes.StrengthModifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public final class BlueprintUtils {

    public static int getInteger(JsonNode parentNode, String key, int defaultValue)
            throws InvalidCardDefinitionException {
        if (!parentNode.has(key))
            return defaultValue;
        else {
            JsonNode node = parentNode.get(key);
            if (!node.isInt())
                throw new InvalidCardDefinitionException("Unknown type in " + key + " field");
            else return node.asInt(defaultValue);
        }
    }


    public static String getString(JsonNode parentNode, String key) {
        if (!parentNode.has(key))
            return null;
        else return parentNode.get(key).textValue();
    }

    public static String getString(JsonNode parentNode, String key, String defaultValue) {
        if (parentNode == null || !parentNode.has(key))
            return defaultValue;
        else
            return parentNode.get(key).textValue();
    }

    public static FilterableSource getFilterable(JsonNode node) throws InvalidCardDefinitionException {
        return new FilterFactory().generateFilter(node.get("filter").textValue());
    }


    public static FilterableSource getFilterable(JsonNode node, String defaultValue)
            throws InvalidCardDefinitionException {
        if (!node.has("filter"))
            return new FilterFactory().generateFilter(defaultValue);
        else return new FilterFactory().generateFilter(node.get("filter").textValue());
    }


    public static boolean getBoolean(JsonNode parentNode, String key, boolean defaultValue)
            throws InvalidCardDefinitionException {
        if (parentNode.has(key)) {
            JsonNode node = parentNode.get(key);
            if (!node.isBoolean())
                throw new InvalidCardDefinitionException("Unknown type in " + key + " field");
            else return node.asBoolean();
        } else {
            return defaultValue;
        }
    }

    public static <T extends Enum<T>> T getEnum(Class<T> enumClass, JsonNode parentNode, String key)
            throws InvalidCardDefinitionException {
        return getEnum(enumClass, parentNode, key, true);
    }

    public static <T extends Enum<T>> T getEnum(Class<T> enumClass, JsonNode parentNode, String key,
                                                boolean nullsAllowed)
            throws InvalidCardDefinitionException {
        if (parentNode.get(key) == null || !parentNode.get(key).isTextual()) {
            if (nullsAllowed) return null;
            else throw new InvalidCardDefinitionException("Unable to process enum value in " + key + " field");
        }
        try {
            return Enum.valueOf(enumClass,
                    parentNode.get(key).textValue().toUpperCase().replaceAll("[ '\\-.]", "_"));
        } catch(Exception exp) {
            throw new InvalidCardDefinitionException(
                    "Unable to process enum value " + parentNode.get(key) + " in " + key + " field");
        }
    }

    public static <T extends Enum<T>> T getEnum(Class<T> enumClass, String value, String key)
            throws InvalidCardDefinitionException {
        if (value == null)
            return null;
        try {
            return Enum.valueOf(enumClass, value.toUpperCase().replaceAll("[ '\\-.]", "_"));
        } catch(Exception exp) {
            throw new InvalidCardDefinitionException("Unable to process enum value " + value + " in " + key + " field");
        }
    }

    public static PlayerSource getPlayerSource(JsonNode parentNode, String key, boolean useYouAsDefault)
            throws InvalidCardDefinitionException {
        String playerString;
        if (parentNode.get(key) == null && useYouAsDefault)
            playerString = "you";
        else playerString = parentNode.get(key).textValue();
        return PlayerResolver.resolvePlayer(playerString);
    }


    public static void validateAllowedFields(JsonNode node, String... fields) throws InvalidCardDefinitionException {
        // Always allowed - type, player, selectingPlayer, targetPlayer
        List<String> allowedFields = Arrays.asList(fields);
        List<String> unrecognizedFields = new LinkedList<>();
        node.fieldNames().forEachRemaining(fieldName -> {
            switch(fieldName) {
                case "player", "selectingPlayer", "targetPlayer", "type":
                    break;
                default:
                    if (!allowedFields.contains(fieldName)) unrecognizedFields.add(fieldName);
                    break;
            }
        });
        if (!unrecognizedFields.isEmpty())
            throw new InvalidCardDefinitionException("Unrecognized field: " + unrecognizedFields.getFirst());
        if (node.has("player") && (node.has("selectingPlayer") || (node.has("targetPlayer"))))
            throw new InvalidCardDefinitionException("Blueprint has both 'player' and either 'selectingPlayer' or 'targetPlayer'");
    }

    public static void validateRequiredFields(JsonNode node, String... fields) throws InvalidCardDefinitionException {
        List<String> keys = new ArrayList<>();
        node.fieldNames().forEachRemaining(keys::add);
        for (String field : fields) {
            if (!keys.contains(field))
                throw new InvalidCardDefinitionException("Missing field: " + field);
        }
    }


    public static ModifierSource getModifier(JsonNode node) throws InvalidCardDefinitionException {
        ModifierSourceProcessorType modifierType = getEnum(ModifierSourceProcessorType.class, node, "type");
        validateAllowedFields(node, modifierType);

        final Requirement[] requirements = RequirementFactory.getRequirements(node);
        final FilterableSource filterableSource;

        switch(modifierType) {
            case GAINICON:
                CardIcon icon = getEnum(CardIcon.class, node, "icon");
                filterableSource = new FilterFactory().parseSTCCGFilter(node.get("filter").textValue());
                return actionContext -> new GainIconModifier(actionContext,
                        filterableSource.getFilterable(actionContext),
                        new RequirementCondition(requirements, actionContext), icon);
            case MODIFYSTRENGTH:
                ValueSource valueSource = ValueResolver.resolveEvaluator(node.get("amount").textValue());
                filterableSource = getFilterable(node);
                return (actionContext) -> {
                    final Evaluator evaluator = valueSource.getEvaluator(actionContext);
                    return new StrengthModifier(actionContext,
                            filterableSource.getFilterable(actionContext),
                            new RequirementCondition(requirements, actionContext), evaluator);
                };
            case OPPONENTMAYNOTDISCARD:
                filterableSource = getFilterable(node);
                return (actionContext) -> new CantDiscardFromPlayByPlayerModifier(
                        actionContext.getSource(), "Can't be discarded by opponent",
                        filterableSource.getFilterable(actionContext), actionContext.getPerformingPlayerId());
            default:
                throw new InvalidCardDefinitionException("Unable to resolve modifier of type: " + modifierType);
        }
    }

    private enum ModifierSourceProcessorType { CANTPLAYCARDS, GAINICON, MODIFYSTRENGTH, OPPONENTMAYNOTDISCARD }

    private static void validateAllowedFields(JsonNode node, ModifierSourceProcessorType modifierType)
            throws InvalidCardDefinitionException {
        switch(modifierType) {
            case CANTPLAYCARDS:
                validateAllowedFields(node, "filter", "requires");
                break;
            case GAINICON:
                validateAllowedFields(node, "filter", "requires", "icon");
                break;
            case MODIFYSTRENGTH:
                validateAllowedFields(node, "filter", "requires", "amount");
                break;
            case OPPONENTMAYNOTDISCARD:
                validateAllowedFields(node, "filter");
                break;
            default:
                throw new InvalidCardDefinitionException("Unable to resolve modifier of type : " + modifierType);
        }
    }

    public static PlayerSource getSelectingPlayerSource(JsonNode parentNode)
            throws InvalidCardDefinitionException {

        if (parentNode == null)
            throw new InvalidCardDefinitionException("Unable to find JsonNode node");
        if (parentNode.has("player") && (parentNode.has("selectingPlayer") || parentNode.has("targetPlayer")))
            throw new InvalidCardDefinitionException("Unable to identify selecting player from JSON blueprint");

        if (parentNode.has("player") && parentNode.get("player").isTextual())
            return PlayerResolver.resolvePlayer(parentNode.get("player").textValue());
        if (parentNode.has("selectingPlayer") && parentNode.get("selectingPlayer").isTextual())
            return PlayerResolver.resolvePlayer(parentNode.get("selectingPlayer").textValue());
        if (parentNode.has("targetPlayer") && parentNode.get("targetPlayer").isTextual())
            return PlayerResolver.resolvePlayer(parentNode.get("targetPlayer").textValue());

        return ActionContext::getPerformingPlayerId;

    }

    public static PlayerSource getTargetPlayerSource(JsonNode parentNode)
            throws InvalidCardDefinitionException {

        if (parentNode == null)
            throw new InvalidCardDefinitionException("Unable to find JsonNode node");
        if (parentNode.has("player") && (parentNode.has("selectingPlayer") || parentNode.has("targetPlayer")))
            throw new InvalidCardDefinitionException("Unable to identify selecting player from JSON blueprint");

        if (parentNode.has("player") && parentNode.get("player").isTextual())
            return PlayerResolver.resolvePlayer(parentNode.get("player").textValue());
        if (parentNode.has("targetPlayer") && parentNode.get("targetPlayer").isTextual())
            return PlayerResolver.resolvePlayer(parentNode.get("targetPlayer").textValue());
        if (parentNode.has("selectingPlayer") && parentNode.get("selectingPlayer").isTextual())
            return PlayerResolver.resolvePlayer(parentNode.get("selectingPlayer").textValue());

        return ActionContext::getPerformingPlayerId;

    }


    public static Function<ActionContext, List<PhysicalCard>> getCardSourceFromZone(PlayerSource player, Zone zone,
                                                                                    String filter)
            throws InvalidCardDefinitionException {
        String sourceMemory = (filter.startsWith("memory(")) ?
                filter.substring(filter.indexOf("(") + 1, filter.lastIndexOf(")")) : null;
        return switch (zone) {
            case HAND, DISCARD, DRAW_DECK -> actionContext -> {
                try {
                    String playerId = player.getPlayerId(actionContext);
                    Player performingPlayer = actionContext.getGame().getPlayer(playerId);
                    return Filters.filter(
                            actionContext.getGameState().getZoneCards(performingPlayer, zone),
                            sourceMemory == null ?
                                    Filters.any : Filters.in(actionContext.getCardsFromMemory(sourceMemory))).stream().toList();
                } catch(PlayerNotFoundException exp) {
                    actionContext.getGame().sendErrorMessage(exp);
                    actionContext.getGame().cancelGame();
                    return null;
                }
            };
            default -> throw new InvalidCardDefinitionException(
                    "getCardSource function not defined for zone " + zone.getHumanReadable());
        };
    }
}