package com.gempukku.stccg.decisions.responses;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ActionSelectionDecisionResponse.class, name = "ACTION"),
        @JsonSubTypes.Type(value = CardSelectionDecisionResponse.class, name = "CARD_SELECTION"),
        @JsonSubTypes.Type(value = IntegerSelectionDecisionResponse.class, name = "INTEGER"),
        @JsonSubTypes.Type(value = MultipleChoiceDecisionResponse.class, name = "MULTIPLE_CHOICE")
})
public interface DecisionResponse {
    int getDecisionId();
}