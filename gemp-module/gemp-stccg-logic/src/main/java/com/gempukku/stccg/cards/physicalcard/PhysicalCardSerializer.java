package com.gempukku.stccg.cards.physicalcard;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class PhysicalCardSerializer extends StdSerializer<PhysicalCard> {

    public PhysicalCardSerializer() {
        this(null);
    }

    public PhysicalCardSerializer(Class<PhysicalCard> t) {
        super(t);
    }

    @Override
    public void serialize(PhysicalCard physicalCard, JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("title", physicalCard.getTitle());
        jsonGenerator.writeStringField("blueprintId", physicalCard.getBlueprintId());
        jsonGenerator.writeNumberField("cardId", physicalCard.getCardId());
        jsonGenerator.writeStringField("owner", physicalCard.getOwnerName());
        jsonGenerator.writeStringField("zone", physicalCard.getZone().name());
        if (physicalCard.getAttachedTo() != null)
            jsonGenerator.writeNumberField("attachedToCardId", physicalCard.getAttachedTo().getCardId());
        if (physicalCard.getStackedOn() != null)
            jsonGenerator.writeNumberField("stackedOnCardId", physicalCard.getStackedOn().getCardId());
        if (physicalCard.getLocationZoneIndex() >= 0)
            jsonGenerator.writeNumberField("locationZoneIndex", physicalCard.getLocationZoneIndex());

        if (!physicalCard.getCardsSeededUnderneath().isEmpty()) {
            jsonGenerator.writeFieldName("cardsSeededUnderneath");
            jsonGenerator.writeStartArray();
            for (PhysicalCard card : physicalCard.getCardsSeededUnderneath())
                jsonGenerator.writeNumber(card.getCardId());
            jsonGenerator.writeEndArray();
        }

        if (physicalCard instanceof AffiliatedCard affiliatedCard)
            jsonGenerator.writeStringField("affiliation", affiliatedCard.getAffiliation().name());
        if (physicalCard instanceof MissionCard mission)
            jsonGenerator.writeBooleanField("completed", mission._completed);
        if (physicalCard instanceof PhysicalShipCard shipCard) {
            if (shipCard.isDocked())
                jsonGenerator.writeNumberField("dockedAtCard", shipCard.getDockedAtCard().getCardId());
            jsonGenerator.writeNumberField("rangeAvailable", shipCard.getRangeAvailable());
        }


        jsonGenerator.writeEndObject();
    }
}