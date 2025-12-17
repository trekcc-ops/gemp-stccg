package com.gempukku.stccg.requirement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = MiscRequirement.class, names = {"cardsindeckcount", "cardsinhandmorethan",
    "hascardindiscard", "hascardinhand", "hascardinplaypile", "lasttribbleplayed", "nextTribbleInSequence",
    "tribblesequencebroken"}),
        @JsonSubTypes.Type(value = ComparatorRequirement.class, names = {"isequal", "isgreaterthan", "isgreaterthanorequal",
        "islessthan", "islessthanorequal", "isnotequal"}),
        @JsonSubTypes.Type(value = PhaseRequirement.class, name = "phase"),
        @JsonSubTypes.Type(value = PlayOutOfSequenceRequirement.class, name = "playOutOfSequenceCondition"),
        @JsonSubTypes.Type(value = ThisCardPresentWithYourCardRequirement.class, names = "thisCardPresentWithYourCard")
})
public interface Requirement {

    default boolean accepts(ActionContext actionContext, DefaultGame cardGame) {
        return isTrue(actionContext.card(), cardGame);
    }

    @JsonIgnore
    default boolean isTrue(PhysicalCard thisCard, DefaultGame cardGame) {
        return false;
    }

    @JsonIgnore
    default Condition getCondition(ActionContext context, PhysicalCard thisCard, DefaultGame cardGame) {
        return null;
    }

}