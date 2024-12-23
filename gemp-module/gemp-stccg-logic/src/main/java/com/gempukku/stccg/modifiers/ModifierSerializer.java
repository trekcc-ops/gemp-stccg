package com.gempukku.stccg.modifiers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class ModifierSerializer extends StdSerializer<Modifier> {

    public ModifierSerializer() {
        this(null);
    }

    public ModifierSerializer(Class<Modifier> t) {
        super(t);
    }

    @Override
    public void serialize(Modifier modifier, JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        if (modifier.getSource() != null)
            jsonGenerator.writeNumberField("cardSource", modifier.getSource().getCardId());
        jsonGenerator.writeStringField("text", modifier.getText());
        jsonGenerator.writeStringField("playerId", modifier.getForPlayer());
        jsonGenerator.writeStringField("modifierType", modifier.getModifierEffect().name());
        // TODO Condition _condition
        // TODO Evaluator _evaluator (currently only in AttributeModifier)
        // TODO Filter _filters (CanPlayCardOutOfSequenceModifier, CantPlayCardsModifier)
        // TODO Filter _sourceFilter (CancelStrengthBonusTargetModifier, CantDiscardFromPlayModifier,
        //  CantReturnToHandModifier)
        // TODO String _playerId (CantDiscardCardsFromHandOrTopOfDeckModifier, CantDiscardFromPlayByPlayerModifier,
        //  PlayerCantPlayCardsModifier) - this is a different playerId from fromPlayer
        // TODO Filterable[] _cardFilter (DiscardFromHandExtraPlayCostModifier)
        // TODO SkillName _skill (GainSkillModifier)
        // TODO Filter _unplayableCardFilter (MayNotBePlayedOnModifier)
        // TODO ModifierFlag _modifierFlag (SpecialFLagModifier)
        jsonGenerator.writeEndObject();
    }
}