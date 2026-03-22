package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.game.DefaultGame;

public class ThisCardPointBoxValueSource implements SingleValueSource {
    @Override
    public Evaluator getEvaluator(DefaultGame cardGame, GameTextContext context) {
        return new ConstantEvaluator(context.card().getPointBoxValue());
    }
}