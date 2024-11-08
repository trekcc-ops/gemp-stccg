package com.gempukku.stccg.game;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.gempukku.stccg.common.JsonUtils;

import java.io.IOException;

public class PlayerOrderSerializer extends StdSerializer<PlayerOrder> {

    public PlayerOrderSerializer() {
        this(null);
    }

    public PlayerOrderSerializer(Class<PlayerOrder> t) {
        super(t);
    }

    @Override
    public void serialize(PlayerOrder playerOrder, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("firstPlayer", playerOrder.getFirstPlayer());
        jsonGenerator.writeBooleanField("isReversed", playerOrder.isReversed());
        JsonUtils.writeArray("turnOrder", playerOrder.getAllPlayers(), jsonGenerator);
        jsonGenerator.writeStringField("currentPlayer", playerOrder.getCurrentPlayer());
        jsonGenerator.writeEndObject();
    }
}