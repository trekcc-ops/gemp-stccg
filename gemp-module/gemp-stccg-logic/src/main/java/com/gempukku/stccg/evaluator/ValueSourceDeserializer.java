package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ValueSourceDeserializer extends StdDeserializer<ValueSource> {

    private static final Map<String, Class<? extends ValueSource>> _typeMapping = new HashMap<>();

    @SuppressWarnings("unused")
    public ValueSourceDeserializer() {
        this(null);
    }

    public ValueSourceDeserializer(Class<?> vc) {
        super(vc);
        addToMapping("range", VariableRangeValueSource.class);
        addToMapping("requires", ConditionalValueSource.class);
        addToMapping("forEachInMemory", ForEachInMemoryValueSource.class);
        addToMapping("limit", LimitValueSource.class);
        addToMapping("forEachInDiscard", CountDiscardValueSource.class);
        addToMapping("max", MaximumValueSource.class);
        addToMapping("min", MinimumValueSource.class);
    }

    private void addToMapping(String type, Class<? extends ValueSource> clazz) {
        _typeMapping.put(type.toLowerCase(), clazz);
    }

    @Override
    public ValueSource deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode object = jp.getCodec().readTree(jp);
        if (object != null)
            return resolveEvaluator(ctxt, object);
        else throw new InvalidCardDefinitionException("Null value source");
    }

    public static ValueSource resolveEvaluator(DeserializationContext ctxt, JsonNode value)
            throws IOException {
        if (value.isInt())
            return new ConstantValueSource(value.asInt());
        if (value.isTextual()) {
            String stringValue = value.textValue();
            if (stringValue.contains("-")) {
                final String[] split = stringValue.split("-", 2);
                final int min = Integer.parseInt(split[0]);
                final int max = Integer.parseInt(split[1]);
                if (min > max || min < 0 || max < 1)
                    throw new InvalidCardDefinitionException("Unable to resolve count: " + stringValue);
                return new NumberRangeValueSource(min, max);
            } else {
                return new ConstantValueSource(Integer.parseInt(stringValue));
            }
        }
        if (value instanceof JsonNode object) {
            JsonNode typeNode = object.get("type");
            if (typeNode == null || !typeNode.isTextual())
                throw new InvalidCardDefinitionException("ValueSource type not defined");
            return deserializeValueSourceFromType(typeNode.textValue(), object, ctxt);
        }
        throw new InvalidCardDefinitionException("Unable to resolve ValueSource");
    }

    static ValueSource deserializeValueSourceFromType(String type, JsonNode object, DeserializationContext ctxt)
            throws IOException {
        Class<? extends ValueSource> clazz = _typeMapping.get(type.toLowerCase());
        if (clazz == null) {
            throw new InvalidCardDefinitionException("Unrecognized type of for ValueSource: " + type);
        } else {
            return ctxt.readTreeAsValue(object, clazz);
        }
    }

}