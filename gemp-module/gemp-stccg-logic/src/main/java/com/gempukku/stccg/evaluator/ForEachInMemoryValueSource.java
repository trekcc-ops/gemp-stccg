package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.game.DefaultGame;

public class ForEachInMemoryValueSource implements SingleValueSource {

    private final String _memory;
    private final int _limit;

    public ForEachInMemoryValueSource(
            @JsonProperty(value = "memory", required = true)
            String memory,
            @JsonProperty(value = "limit", required = true)
            int limit) {
        _memory = memory;
        _limit = limit;
    }

    @Override
    public Evaluator getEvaluator(DefaultGame cardGame, GameTextContext context) {
        return new Evaluator() {
            @Override
            public float evaluateExpression(DefaultGame cardGame) {
                final int count = context.getCardIdsFromMemory(_memory).size();
                return Math.min(_limit, count);
            }
        };
    }
}