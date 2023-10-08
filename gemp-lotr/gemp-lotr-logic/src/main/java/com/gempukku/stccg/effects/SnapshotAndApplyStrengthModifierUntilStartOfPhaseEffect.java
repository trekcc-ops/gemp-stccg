package com.gempukku.stccg.effects;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.Filterable;
import com.gempukku.stccg.common.Phase;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.StrengthModifier;
import com.gempukku.stccg.evaluator.Evaluator;

import java.util.Collection;

public class SnapshotAndApplyStrengthModifierUntilStartOfPhaseEffect extends AbstractEffect {
    private final PhysicalCard _source;
    private final Filterable[] _filters;
    private final Evaluator _evaluator;
    private final Phase _phase;

    public SnapshotAndApplyStrengthModifierUntilStartOfPhaseEffect(PhysicalCard source, Evaluator evaluator, Phase phase, Filterable... filter) {
        _source = source;
        _evaluator = evaluator;
        _phase = phase;
        _filters = filter;
    }

    @Override
    public String getText(DefaultGame game) {
        return "Apply strength modifier";
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        final Collection<PhysicalCard> affectedCards = Filters.filterActive(game, _filters);
        for (PhysicalCard physicalCard : affectedCards) {
            final int modifier = _evaluator.evaluateExpression(game, physicalCard);
            if (modifier != 0)
                game.getModifiersEnvironment().addUntilStartOfPhaseModifier(
                        new StrengthModifier(_source, Filters.sameCard(physicalCard), modifier), _phase);
        }

        return new FullEffectResult(true);
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return true;
    }
}
