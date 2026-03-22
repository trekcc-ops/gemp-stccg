package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;

import java.io.IOException;

public class BasicSingleValueSourceDeserializer extends StdDeserializer<BasicSingleValueSource> {

    @SuppressWarnings("unused")
    public BasicSingleValueSourceDeserializer() {
        this(null);
    }

    public BasicSingleValueSourceDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public BasicSingleValueSource deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode object = jp.getCodec().readTree(jp);
        if (object != null)
            return resolveEvaluator(ctxt, object);
        else throw new InvalidCardDefinitionException("Null value source");
    }

    public static BasicSingleValueSource resolveEvaluator(DeserializationContext ctxt, JsonNode value)
            throws IOException {
        if (value.isInt())
            return new ConstantValueSource(value.asInt());
        if (value.isTextual()) {
            String stringValue = value.textValue();
            if (!stringValue.contains("-")) {
                return new ConstantValueSource(Integer.parseInt(stringValue));
            }
        }
        throw new InvalidCardDefinitionException("Unable to resolve ValueSource");
    }

}