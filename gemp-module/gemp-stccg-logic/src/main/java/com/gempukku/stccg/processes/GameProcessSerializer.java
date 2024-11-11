package com.gempukku.stccg.processes;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.gempukku.stccg.processes.st1e.SimultaneousGameProcess;

import java.io.IOException;

public class GameProcessSerializer extends StdSerializer<GameProcess> {
    
    public GameProcessSerializer() {
        this(null);
    }
    
    public GameProcessSerializer(Class<GameProcess> t) {
        super(t);
    }
    
    @Override
    public void serialize(GameProcess process, JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("className", process.getClass().getSimpleName());
        jsonGenerator.writeNumberField("consecutivePasses", process.getConsecutivePasses());
        jsonGenerator.writeBooleanField("isFinished", process.isFinished());

        if (process instanceof SimultaneousGameProcess simulProcess)
            jsonGenerator.writeObjectField("playersParticipating", simulProcess.getPlayersParticipating());
        jsonGenerator.writeEndObject();
    }
}