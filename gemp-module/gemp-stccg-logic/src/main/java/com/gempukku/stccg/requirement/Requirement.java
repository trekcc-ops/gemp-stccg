package com.gempukku.stccg.requirement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = OrRequirement.class, name = "or"),
        @JsonSubTypes.Type(value = ThisCardIsFacingDilemmaRequirement.class, name = "thisCardIsFacingDilemma"),
        @JsonSubTypes.Type(value = ThisCardIsOnPlanetRequirement.class, name = "thisCardIsOnPlanet"),
        @JsonSubTypes.Type(value = CardInPlayRequirement.class, name = "cardInPlay"),
        @JsonSubTypes.Type(value = MiscRequirement.class, names = {"cardsindeckcount", "cardsinhandmorethan",
    "hascardindiscard", "hascardinhand", "hascardinplaypile", "lasttribbleplayed", "nextTribbleInSequence",
    "tribblesequencebroken"}),
        @JsonSubTypes.Type(value = PhaseRequirement.class, name = "phase"),
        @JsonSubTypes.Type(value = PlayOutOfSequenceRequirement.class, name = "playOutOfSequenceCondition"),
        @JsonSubTypes.Type(value = ThisCardPresentWithCardRequirement.class, names = "thisCardPresentWithCard"),
        @JsonSubTypes.Type(value = YourTurnRequirement.class, name = "yourTurn")
})
public interface Requirement {

    boolean accepts(ActionContext context, DefaultGame cardGame);

    @JsonIgnore
    default Condition getCondition(ActionContext context, PhysicalCard thisCard, DefaultGame cardGame) {
        return null;
    }

}