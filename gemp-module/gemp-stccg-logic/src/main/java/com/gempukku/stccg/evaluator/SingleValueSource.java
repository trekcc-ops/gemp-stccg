package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type",
        defaultImpl = BasicSingleValueSource.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ForEachInMemoryValueSource.class, name = "forEachInMemory"),
        @JsonSubTypes.Type(value = CountDiscardValueSource.class, name = "forEachInDiscard"),
        @JsonSubTypes.Type(value = MaximumValueSource.class, name = "max"),
        @JsonSubTypes.Type(value = MinimumValueSource.class, name = "min"),
        @JsonSubTypes.Type(value = SkillDotCountValueSource.class, name = "skillDotCount"),
        @JsonSubTypes.Type(value = ThisCardPointBoxValueSource.class, name = "thisCardPointBoxValue")
})
public interface SingleValueSource extends ValueSource {

    default int getMinimum(DefaultGame cardGame, ActionContext actionContext) {
        return evaluateExpression(cardGame, actionContext);
    }

    default int getMaximum(DefaultGame cardGame, ActionContext actionContext) {
        return evaluateExpression(cardGame, actionContext);
    }

    int evaluateExpression(DefaultGame cardGame, ActionContext actionContext);

}