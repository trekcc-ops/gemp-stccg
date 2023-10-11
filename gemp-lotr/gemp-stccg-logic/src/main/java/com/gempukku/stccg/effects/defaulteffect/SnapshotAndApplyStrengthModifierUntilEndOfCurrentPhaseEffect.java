package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.StrengthModifier;

public class SnapshotAndApplyStrengthModifierUntilEndOfCurrentPhaseEffect extends DefaultEffect {
    private final PhysicalCard _source;
    private final Filterable[] _filters;
    private final Evaluator _evaluator;
    private final DefaultGame _game;

    public SnapshotAndApplyStrengthModifierUntilEndOfCurrentPhaseEffect(DefaultGame game, PhysicalCard source, Evaluator evaluator, Filterable... filter) {
        _source = source;
        _evaluator = evaluator;
        _filters = filter;
        _game = game;
    }

    @Override
    public String getText() {
        return "Apply strength modifier";
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        for (PhysicalCard physicalCard : Filters.filterActive(_game, _filters)) {
            final int modifier = _evaluator.evaluateExpression(_game, physicalCard);
            if (modifier != 0)
                _game.getModifiersEnvironment().addUntilEndOfPhaseModifier(
                        new StrengthModifier(_source, Filters.sameCard(physicalCard), modifier), _game.getGameState().getCurrentPhase());
        }

        return new FullEffectResult(true);
    }

    @Override
    public boolean isPlayableInFull() {
        return true;
    }
}
