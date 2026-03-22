package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.Requirement;

import java.util.List;

public class ConditionalValueSource implements ValueSource {

    private final ValueSource _trueValue;
    private final ValueSource _falseValue;
    private final List<Requirement> _conditions;

    @JsonCreator
    private ConditionalValueSource(
        @JsonProperty(value = "ifCondition", required = true)
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        List<Requirement> conditions,
        @JsonProperty(value = "valueIfTrue", required = true)
        ValueSource trueValue,
        @JsonProperty(value = "valueIfFalse", required = true)
        ValueSource falseValue
    ) {
        _trueValue = trueValue;
        _falseValue = falseValue;
        _conditions = conditions;
    }
    @Override
    public int getMinimum(DefaultGame cardGame, GameTextContext actionContext) {
        ValueSource sourceToUse = (actionContext.acceptsAllRequirements(cardGame, _conditions)) ?
                _trueValue : _falseValue;
        return sourceToUse.getMinimum(cardGame, actionContext);
    }

    @Override
    public int getMaximum(DefaultGame cardGame, GameTextContext actionContext) {
        ValueSource sourceToUse = (actionContext.acceptsAllRequirements(cardGame, _conditions)) ?
                _trueValue : _falseValue;
        return sourceToUse.getMaximum(cardGame, actionContext);
    }
}