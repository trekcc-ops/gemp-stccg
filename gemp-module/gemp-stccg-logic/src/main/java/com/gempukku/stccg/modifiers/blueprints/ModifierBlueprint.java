package com.gempukku.stccg.modifiers.blueprints;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.modifiers.Modifier;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = GainIconModifierBlueprint.class, name = "gainIcon"),
        @JsonSubTypes.Type(value = StrengthModifierBlueprint.class, name = "modifyStrength")
})
public interface ModifierBlueprint {

    Modifier getModifier(ActionContext actionContext);
}