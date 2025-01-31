package com.gempukku.stccg.cards.blueprints;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public final class BlueprintUtils {


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