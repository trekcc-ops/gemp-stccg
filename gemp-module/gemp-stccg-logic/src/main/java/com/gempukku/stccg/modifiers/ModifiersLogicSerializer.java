package com.gempukku.stccg.modifiers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.gempukku.stccg.common.JsonUtils;

import java.io.IOException;

public class ModifiersLogicSerializer extends StdSerializer<ModifiersLogic> {

    public ModifiersLogicSerializer() {
        this(null);
    }

    public ModifiersLogicSerializer(Class<ModifiersLogic> t) {
        super(t);
    }

    @Override
    public void serialize(ModifiersLogic modifiers, JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        JsonUtils.writeArray("modifiers", modifiers.getModifiers(), jsonGenerator);
        jsonGenerator.writeEndObject();
    }
}