package com.gempukku.stccg.requirement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

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

    default boolean accepts(ActionContext actionContext, DefaultGame cardGame) {
        try {
            return isTrue(actionContext.getPerformingCard(cardGame), cardGame);
        } catch(InvalidGameLogicException exp) {
            cardGame.sendErrorMessage(exp);
            return false;
        }
    }

    @JsonIgnore
    default boolean isTrue(PhysicalCard thisCard, DefaultGame cardGame) {
        return false;
    }

}