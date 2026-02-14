package com.gempukku.stccg.cards;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.blueprints.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.requirement.trigger.TriggerChecker;

import java.util.ArrayList;
import java.util.List;

public class SpecialResponseActionSkill extends Skill {

    private final TriggerActionBlueprint _actionBlueprint;
    private final String _skillText;

    @JsonCreator
    private SpecialResponseActionSkill(@JsonProperty("action")SubActionBlueprint subActionBlueprint,
                                       @JsonProperty("text") String skillText,
                                       @JsonProperty("trigger") TriggerChecker triggerChecker,
                                       @JsonProperty(value = "optional", required = true) Boolean optional)
            throws InvalidCardDefinitionException {
        _skillText = skillText;
        _actionBlueprint = optional ?
                new OptionalTriggerActionBlueprint(null, false, triggerChecker,
                        new ArrayList<>(), new ArrayList<>(), List.of(subActionBlueprint), "you") :
                new RequiredTriggerActionBlueprint(false, triggerChecker, new ArrayList<>(), new ArrayList<>(),
                        List.of(subActionBlueprint), "you");
    }

    public ActionBlueprint getActionBlueprint(PhysicalCard thisCard) {
        return _actionBlueprint;
    }

}