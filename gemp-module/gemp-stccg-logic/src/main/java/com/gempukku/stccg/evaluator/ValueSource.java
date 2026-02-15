package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type",
    defaultImpl = BasicValueSource.class)
@JsonSubTypes({@JsonSubTypes.Type(value = VariableRangeValueSource.class, name = "range"),
        @JsonSubTypes.Type(value = ConditionalValueSource.class, name = "requires"),
        @JsonSubTypes.Type(value = ForEachInMemoryValueSource.class, name = "forEachInMemory"),
        @JsonSubTypes.Type(value = CountDiscardValueSource.class, name = "forEachInDiscard"),
        @JsonSubTypes.Type(value = MaximumValueSource.class, name = "max"),
        @JsonSubTypes.Type(value = MinimumValueSource.class, name = "min"),
        @JsonSubTypes.Type(value = SkillDotCountValueSource.class, name = "skillDotCount"),
        @JsonSubTypes.Type(value = ThisCardPointBoxValueSource.class, name = "thisCardPointBoxValue")
})
public interface ValueSource {

    int getMinimum(DefaultGame cardGame, ActionContext actionContext) throws InvalidGameLogicException;

    int getMaximum(DefaultGame cardGame, ActionContext actionContext) throws InvalidGameLogicException;

}