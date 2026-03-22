package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;

import java.io.IOException;

public class BasicValueSourceDeserializer extends StdDeserializer<BasicValueSource> {

    @SuppressWarnings("unused")
    public BasicValueSourceDeserializer() {
        this(null);
    }

    public BasicValueSourceDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public BasicValueSource deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode object = jp.getCodec().readTree(jp);
        if (object != null)
            return resolveEvaluator(ctxt, object);
        else throw new InvalidCardDefinitionException("Null value source");
    }

    public static BasicValueSource resolveEvaluator(DeserializationContext ctxt, JsonNode value)
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
        throw new InvalidCardDefinitionException("Unable to resolve ValueSource");
    }

}