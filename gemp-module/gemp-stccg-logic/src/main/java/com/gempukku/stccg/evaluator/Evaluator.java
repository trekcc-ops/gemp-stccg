package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gempukku.stccg.game.DefaultGame;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "type")
public abstract class Evaluator {
    public abstract float evaluateExpression(DefaultGame cardGame);

}