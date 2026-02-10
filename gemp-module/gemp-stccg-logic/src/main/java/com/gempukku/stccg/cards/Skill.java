package com.gempukku.stccg.cards;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = RegularSkill.class, name = "regular"),
        @JsonSubTypes.Type(value = ModifierSkill.class, name = "specialModifier")
})
public abstract class Skill {
}