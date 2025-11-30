package com.gempukku.stccg.modifiers.blueprints;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.Modifier;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AddCunningModifierBlueprint.class, name = "addCunning"),
        @JsonSubTypes.Type(value = AddStrengthModifierBlueprint.class, name = "addStrength"),
        @JsonSubTypes.Type(value = GainIconModifierBlueprint.class, name = "gainIcon"),
        @JsonSubTypes.Type(value = GainSkillModifierBlueprint.class, name = "gainSkill")
})
public interface ModifierBlueprint {
    Modifier getModifier(DefaultGame cardGame, ActionContext actionContext);

}