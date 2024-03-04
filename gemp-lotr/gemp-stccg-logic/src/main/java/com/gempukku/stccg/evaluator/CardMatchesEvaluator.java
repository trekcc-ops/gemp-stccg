package com.gempukku.stccg.evaluator;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

public class CardMatchesEvaluator extends Evaluator {
    private final Filterable[] _filters;
    private final Evaluator _matches;
    private final int _default;

    public CardMatchesEvaluator(ActionContext context, int defaultValue, Evaluator matches, Filterable... filters) {
        super(context);
        _default = defaultValue;
        _matches = matches;
        _filters = filters;
    }

    @Override
    public int evaluateExpression(DefaultGame game, PhysicalCard self) {
        return Filters.and(_filters).accepts(_game, self) ? _matches.evaluateExpression(_game, self) : _default;
    }
}
