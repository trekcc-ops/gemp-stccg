package com.gempukku.stccg.cards;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

import java.io.IOException;
import java.util.Map;

public class ActionContextSerializer extends StdSerializer<ActionContext> {

    public ActionContextSerializer() {
        this(null);
    }

    public ActionContextSerializer(Class<ActionContext> t) {
        super(t);
    }

    @Override
    public void serialize(ActionContext context, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        jsonGenerator.writeStartObject();
        if (context.getParentContext() != context)
            jsonGenerator.writeObjectField("parentContext", context.getParentContext());
        jsonGenerator.writeStringField("performingPlayer", context.getPerformingPlayerId());
        jsonGenerator.writeNumberField("source", context.getSource().getCardId());

        if (!context.getCardMemory().isEmpty()) {
            jsonGenerator.writeObjectFieldStart("cardMemory");
            for (Map.Entry<String, PhysicalCard> entry : context.getCardMemory().entries()) {
                jsonGenerator.writeNumberField(entry.getKey(), entry.getValue().getCardId());
            }
            jsonGenerator.writeEndObject();
        }

        if (!context.getValueMemory().isEmpty()) {
            jsonGenerator.writeObjectFieldStart("valueMemory");
            for (Map.Entry<String, String> entry : context.getValueMemory().entrySet()) {
                jsonGenerator.writeStringField(entry.getKey(), entry.getValue());
            }
            jsonGenerator.writeEndObject();
        }

        jsonGenerator.writeEndObject();
    }
}