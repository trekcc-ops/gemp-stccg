package com.gempukku.stccg.requirement;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gempukku.stccg.cards.ActionContext;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = MiscRequirement.class, names = {"cardsindeckcount", "cardsinhandmorethan",
    "hascardindiscard", "hascardinhand", "hascardinplaypile", "lasttribbleplayed", "nextTribbleInSequence",
    "tribblesequencebroken"}),
        @JsonSubTypes.Type(value = ComparatorRequirement.class, names = {"isequal", "isgreaterthan", "isgreaterthanorequal",
        "islessthan", "islessthanorequal", "isnotequal"}),
        @JsonSubTypes.Type(value = PlayOutOfSequenceCondition.class, name = "playOutOfSequenceCondition"),
        @JsonSubTypes.Type(value = ThisCardPresentWithYourCardRequirement.class, names = "thisCardPresentWithYourCard")
})
public interface Requirement {

    boolean accepts(ActionContext actionContext);

}