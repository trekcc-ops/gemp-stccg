package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.game.DefaultGame;

public class MinimumValueSource implements SingleValueSource {

    private final SingleValueSource _first;
    private final SingleValueSource _second;

    public MinimumValueSource(
            @JsonProperty(value = "first", required = true)
            SingleValueSource first,
            @JsonProperty(value = "second", required = true)
            SingleValueSource second) {
        _first = first;
        _second = second;
    }

    @Override
    public Evaluator getEvaluator(DefaultGame cardGame, GameTextContext context) {
        return new Evaluator() {

            @Override
            public float evaluateExpression(DefaultGame cardGame) {
                return Math.min(
                        _first.evaluateExpression(cardGame, context),
                        _second.evaluateExpression(cardGame, context)
                );
            }
        };
    }
}