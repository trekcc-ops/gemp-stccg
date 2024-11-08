package com.gempukku.stccg.processes.st1e;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class ST1EProcessSerializer extends StdSerializer<ST1EGameProcess> {

    public ST1EProcessSerializer() {
        this(null);
    }

    public ST1EProcessSerializer(Class<ST1EGameProcess> t) {
        super(t);
    }

    @Override
    public void serialize(ST1EGameProcess gameProcess, JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("class", gameProcess.getClass().getSimpleName());
        jsonGenerator.writeObjectField("playersParticipating", gameProcess._playersParticipating);
        if (gameProcess instanceof ST1EMissionSeedPhaseProcess process)
            jsonGenerator.writeNumberField("consecutivePasses", process.getConsecutivePasses());
        if (gameProcess instanceof ST1EFacilitySeedPhaseProcess process)
            jsonGenerator.writeNumberField("consecutivePasses", process.getConsecutivePasses());
        jsonGenerator.writeEndObject();
    }
}