package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.requirement.Requirement;

import java.util.List;

public class ConditionalValueSource implements ValueSource {

    private final ValueSource _trueValue;
    private final ValueSource _falseValue;
    private final List<Requirement> _conditions;

    public ConditionalValueSource(
            @JsonProperty(value = "requires", required = true)
        List<Requirement> conditions,
        @JsonProperty(value = "true", required = true)
        ValueSource trueValue,
        @JsonProperty(value = "false", required = true)
        ValueSource falseValue
    ) {
        _trueValue = trueValue;
        _falseValue = falseValue;
        _conditions = conditions;
    }
    @Override
    public int getMinimum(DefaultGame cardGame, ActionContext actionContext) throws InvalidGameLogicException {
        ValueSource sourceToUse = (actionContext.acceptsAllRequirements(cardGame, _conditions)) ?
                _trueValue : _falseValue;
        return sourceToUse.getMinimum(cardGame, actionContext);
    }

    @Override
    public int getMaximum(DefaultGame cardGame, ActionContext actionContext) throws InvalidGameLogicException {
        ValueSource sourceToUse = (actionContext.acceptsAllRequirements(cardGame, _conditions)) ?
                _trueValue : _falseValue;
        return sourceToUse.getMaximum(cardGame, actionContext);
    }
}