package com.gempukku.stccg.modifiers.blueprints;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.modifiers.Modifier;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = GainIconModifierBlueprint.class, name = "gainIcon")
})
public interface ModifierBlueprint {

    Modifier getModifier(ActionContext actionContext);
}