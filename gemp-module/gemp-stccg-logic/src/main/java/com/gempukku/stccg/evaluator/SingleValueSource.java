package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

public interface SingleValueSource extends ValueSource {

    float evaluateExpression(DefaultGame cardGame, ActionContext actionContext) throws InvalidGameLogicException;
}