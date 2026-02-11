package com.gempukku.stccg.cards;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NullSkill extends Skill {

    // Dummy class designed for skills that aren't ready to be implemented yet

    @JsonCreator
    public NullSkill(@JsonProperty("text") String text) {
    }

}