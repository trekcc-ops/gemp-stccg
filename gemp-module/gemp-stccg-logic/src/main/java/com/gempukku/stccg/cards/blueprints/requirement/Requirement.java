package com.gempukku.stccg.cards.blueprints.requirement;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.game.PlayerNotFoundException;
import com.gempukku.stccg.modifiers.blueprints.GainIconModifierBlueprint;

import java.util.Objects;

/*@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = MiscRequirement.class, names = {"cardsindeckcount", "cardsinhandmorethan",
    "hascardindiscard", "hascardinhand", "hascardinplaypile", "lasttribbleplayed", "nextTribbleInSequence",
    "tribblesequencebroken"}),
        @JsonSubTypes.Type(value = ComparatorRequirement.class, names = {"isequal", "isgreaterthan", "isgreaterthanorequal",
        "islessthan", "islessthanorequal", "isnotequal"}),
        @JsonSubTypes.Type(value = PlayOutOfSequenceCondition.class, name = "playOutOfSequenceCondition") */
/*        @JsonSubTypes.Type(value = IsOwnerRequirement.class, name = "isOwner"),
        @JsonSubTypes.Type(value = NotRequirement.class, name = "not"),
        @JsonSubTypes.Type(value = OrRequirement.class, name = "or"),
        @JsonSubTypes.Type(value = PerTurnLimitRequirement.class, name = "perTurnLimit") */
// })
public interface Requirement {

    boolean accepts(ActionContext actionContext);

}