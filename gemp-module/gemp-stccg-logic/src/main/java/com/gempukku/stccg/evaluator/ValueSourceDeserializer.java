package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.requirement.Requirement;

import java.io.IOException;
import java.util.*;

public class ValueSourceDeserializer extends StdDeserializer<ValueSource> {

    public ValueSourceDeserializer() {
        this(null);
    }

    public ValueSourceDeserializer(Class<?> vc) {
        super(vc);
    }

    private static int getInteger(JsonNode parentNode, String key, int defaultValue)
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

    private static String getString(JsonNode parentNode, String key, String defaultValue) {
        if (parentNode == null || !parentNode.has(key))
            return defaultValue;
        else
            return parentNode.get(key).textValue();
    }

    private static void validateAllowedFields(JsonNode node, String... fields) throws InvalidCardDefinitionException {
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

    @Override
    public ValueSource deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode object = jp.getCodec().readTree(jp);
        if (object != null)
            return resolveEvaluator(ctxt, object);
        else throw new InvalidCardDefinitionException("Null value source");
    }

    private static ValueSource resolveEvaluator(DeserializationContext ctxt, JsonNode node, int defaultValue)
            throws IOException {
        return Objects.requireNonNullElse(resolveEvaluator(ctxt, node), new ConstantValueSource(defaultValue));
    }

    public static ValueSource resolveEvaluator(String stringValue) throws InvalidCardDefinitionException {
        if (stringValue.contains("-")) {
            final String[] split = stringValue.split("-", 2);
            final int min = Integer.parseInt(split[0]);
            final int max = Integer.parseInt(split[1]);
            if (min > max || min < 0 || max < 1)
                throw new InvalidCardDefinitionException("Unable to resolve count: " + stringValue);
            return new NumberRangeValueSource(min, max);
        } else
            return new ConstantValueSource(Integer.parseInt(stringValue));
    }

    public static ValueSource resolveEvaluator(DeserializationContext ctxt, JsonNode value)
            throws IOException {
        if (value.isInt())
            return new ConstantValueSource(value.asInt());
        if (value.isTextual())
            return resolveEvaluator(value.textValue());
        if (value instanceof JsonNode object) {
            JsonNode typeNode = object.get("type");
            if (typeNode == null || !typeNode.isTextual())
                throw new InvalidCardDefinitionException("ValueResolver type not defined");
            String type = typeNode.textValue();
            if (type.equalsIgnoreCase("range")) {
                validateAllowedFields(object, "from", "to");
                ValueSource fromValue = resolveEvaluator(ctxt, object.get("from"));
                ValueSource toValue = resolveEvaluator(ctxt, object.get("to"));
                return new VariableRangeValueSource(fromValue, toValue);
            } else if (type.equalsIgnoreCase("requires")) {
                validateAllowedFields(object, "requires", "true", "false");
                JsonNode requiresArray = object.get("requires");
                List<Requirement> conditions = new ArrayList<>();
                if (requiresArray.isArray()) {
                    for (JsonNode requirement : requiresArray) {
                        conditions.add(ctxt.readTreeAsValue(requirement, Requirement.class));
                    }
                } else {
                    conditions.add(ctxt.readTreeAsValue(requiresArray, Requirement.class));
                }
                ValueSource trueValue = resolveEvaluator(ctxt, object.get("true"));
                ValueSource falseValue = resolveEvaluator(ctxt, object.get("false"));
                return new ConditionalValueSource(trueValue, falseValue, conditions);
            } else if (type.equalsIgnoreCase("forEachInMemory")) {
                validateAllowedFields(object, "memory", "limit");
                final String memory = object.get("memory").textValue();
                final int limit = ctxt.readTreeAsValue(object.get("limit"), Integer.class); // Set to MAX_VALUE if fails
                return new ForEachInMemoryValueSource(memory, limit);
            } else if (type.equalsIgnoreCase("limit")) {
                return ctxt.readTreeAsValue(object, LimitValueSource.class);
            } else if (type.equalsIgnoreCase("forEachInDiscard")) {
                return ctxt.readTreeAsValue(object, CountDiscardValueSource.class);
            } else if (type.equalsIgnoreCase("max")) {
                return ctxt.readTreeAsValue(object, MaximumValueSource.class);
            } else if (type.equalsIgnoreCase("min")) {
                return ctxt.readTreeAsValue(object, MinimumValueSource.class);
            }
            throw new InvalidCardDefinitionException("Unrecognized type of an evaluator " + type);
        }
        throw new InvalidCardDefinitionException("Unable to resolve an evaluator");
    }


}