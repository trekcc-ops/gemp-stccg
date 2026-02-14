package com.gempukku.stccg.cards;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.blueprints.ActionBlueprint;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public class SpecialResponseActionSkill extends Skill {

    private final ActionBlueprint _actionBlueprint;
    private final String _skillText;
    private final boolean _optional;

    @JsonCreator
    private SpecialResponseActionSkill(@JsonProperty("action") ActionBlueprint subActionBlueprint,
                                       @JsonProperty("text") String skillText,
                                       @JsonProperty(value = "optional", required = true) Boolean optional) {
        _skillText = skillText;
        _actionBlueprint = subActionBlueprint;
        _optional = optional;
    }

    public ActionBlueprint getActionBlueprint(PhysicalCard thisCard) {
        return _actionBlueprint;
    }

    public boolean isOptional() {
        return _optional;
    }

}