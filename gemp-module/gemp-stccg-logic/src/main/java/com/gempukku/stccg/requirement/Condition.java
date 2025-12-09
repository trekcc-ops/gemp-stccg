package com.gempukku.stccg.requirement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gempukku.stccg.game.DefaultGame;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AndCondition.class, name = "and"),
        @JsonSubTypes.Type(value = CardInPlayCondition.class, name = "cardInPlay"),
        @JsonSubTypes.Type(value = EndOfThisTurnCondition.class, name = "endOfThisTurn"),
        @JsonSubTypes.Type(value = FacingDilemmaCondition.class, name = "facingDilemma"),
        @JsonSubTypes.Type(value = LastTribblePlayedCondition.class, name = "lastTribblePlayed"),
        @JsonSubTypes.Type(value = NextTribbleInSequenceCondition.class, name = "nextTribbleInSequence"),
        @JsonSubTypes.Type(value = PresentWithYourCardCondition.class, name = "presentWithYourCard"),
        @JsonSubTypes.Type(value = ThisCardAtMissionCondition.class, name = "thisCardAtMission"),
        @JsonSubTypes.Type(value = TribbleSequenceBrokenCondition.class, name = "tribbleSequenceBroken"),
        @JsonSubTypes.Type(value = TrueCondition.class, name = "true"),
            // TODO - Need to fix
        @JsonSubTypes.Type(value = HigherScoreThanAllOtherPlayersCondition.class, name = "higherScoreThanAllOtherPlayers"),
        @JsonSubTypes.Type(value = MoreCardsInHandThanAllOtherPlayersCondition.class, name = "higherScoreThanAllOtherPlayers")
})
public interface Condition {
    @JsonIgnore
    boolean isFulfilled(DefaultGame cardGame);
}