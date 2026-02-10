package com.gempukku.stccg.requirement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ThisCardIsFacingDilemmaRequirement.class, name = "thisCardIsFacingDilemma"),
        @JsonSubTypes.Type(value = CardInPlayRequirement.class, name = "cardInPlay"),
        @JsonSubTypes.Type(value = MiscRequirement.class, names = {"cardsindeckcount", "cardsinhandmorethan",
    "hascardindiscard", "hascardinhand", "hascardinplaypile", "lasttribbleplayed", "nextTribbleInSequence",
    "tribblesequencebroken"}),
        @JsonSubTypes.Type(value = ComparatorRequirement.class, names = {"isequal", "isgreaterthan", "isgreaterthanorequal",
        "islessthan", "islessthanorequal", "isnotequal"}),
        @JsonSubTypes.Type(value = PhaseRequirement.class, name = "phase"),
        @JsonSubTypes.Type(value = PlayOutOfSequenceRequirement.class, name = "playOutOfSequenceCondition"),
        @JsonSubTypes.Type(value = ThisCardPresentWithYourCardRequirement.class, names = "thisCardPresentWithYourCard"),
        @JsonSubTypes.Type(value = YourTurnRequirement.class, name = "yourTurn")
})
public interface Requirement {

    boolean accepts(ActionContext context, DefaultGame cardGame);

    @JsonIgnore
    default Condition getCondition(ActionContext context, PhysicalCard thisCard, DefaultGame cardGame) {
        return null;
    }

}