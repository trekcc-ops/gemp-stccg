package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.game.DefaultGame;

public abstract class Evaluator implements ValueSource {
    protected final DefaultGame _game;
    protected Evaluator(DefaultGame game) { _game = game; }
    protected Evaluator(ActionContext actionContext) { _game = actionContext.getGame(); }
    public abstract int evaluateExpression(DefaultGame game, PhysicalCard cardAffected);
    @Override
    public Evaluator getEvaluator(ActionContext actionContext) { return this; }
}
