package com.gempukku.stccg.cards.blueprints;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.resolver.PlayerResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.FilterFactory;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.PlayerNotFoundException;

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


    public static String getString(JsonNode parentNode, String key, String defaultValue) {
        if (parentNode == null || !parentNode.has(key))
            return defaultValue;
        else
            return parentNode.get(key).textValue();
    }

    public static FilterBlueprint getFilterable(JsonNode node) throws InvalidCardDefinitionException {
        return new FilterFactory().generateFilter(node.get("filter").textValue());
    }


    public static FilterBlueprint getFilterable(JsonNode node, String defaultValue)
            throws InvalidCardDefinitionException {
        if (!node.has("filter"))
            return new FilterFactory().generateFilter(defaultValue);
        else return new FilterFactory().generateFilter(node.get("filter").textValue());
    }


    public static <T extends Enum<T>> T getEnum(Class<T> enumClass, JsonNode parentNode, String key)
            throws InvalidCardDefinitionException {
        if (parentNode.get(key) == null || !parentNode.get(key).isTextual()) {
            return null;
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


}