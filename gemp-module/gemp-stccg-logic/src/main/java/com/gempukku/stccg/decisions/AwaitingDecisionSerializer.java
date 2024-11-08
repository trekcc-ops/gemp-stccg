package com.gempukku.stccg.decisions;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class AwaitingDecisionSerializer extends StdSerializer<AwaitingDecision> {

    public AwaitingDecisionSerializer() {
        this(null);
    }

    public AwaitingDecisionSerializer(Class<AwaitingDecision> t) {
        super(t);
    }

    @Override
    public void serialize(AwaitingDecision decision, JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("id", decision.getAwaitingDecisionId());
        jsonGenerator.writeStringField("text", decision.getText());
        jsonGenerator.writeStringField("type", decision.getDecisionType().name());
        jsonGenerator.writeStringField("decidingPlayerId", decision.getDecidingPlayer().getPlayerId());
        jsonGenerator.writeObjectField("parameters", decision.getDecisionParameters());

/*        if (decision instanceof ActionDecision actionDecision)
            JsonUtils.writeArray("actions", actionDecision.getActions(), jsonGenerator); */ // TODO

        jsonGenerator.writeEndObject();
    }
}