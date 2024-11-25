package com.gempukku.stccg.cards;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

import java.io.IOException;

public class AwayTeamSerializer extends StdSerializer<AwayTeam> {

    public AwayTeamSerializer() {
        this(null);
    }

    public AwayTeamSerializer(Class<AwayTeam> t) {
        super(t);
    }

    @Override
    public void serialize(AwayTeam awayTeam, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("locationZoneIndex", awayTeam.getLocationZoneIndex());
        jsonGenerator.writeStringField("playerId", awayTeam.getPlayerId());
        writeCardIdArray("cardsInAwayTeam", awayTeam.getCards(), jsonGenerator);
        jsonGenerator.writeEndObject();
    }

    private void writeCardIdArray(String fieldName, Iterable<? extends PhysicalCard> cards,
                                  JsonGenerator jsonGenerator) throws IOException {
        jsonGenerator.writeArrayFieldStart(fieldName);
        for (PhysicalCard card : cards)
            jsonGenerator.writeNumber(card.getCardId());
        jsonGenerator.writeEndArray();
    }
}