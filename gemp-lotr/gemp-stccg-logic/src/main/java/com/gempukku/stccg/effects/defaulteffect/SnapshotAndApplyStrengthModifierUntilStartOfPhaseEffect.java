package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.StrengthModifier;
import com.gempukku.stccg.evaluator.Evaluator;

import java.util.Collection;

public class SnapshotAndApplyStrengthModifierUntilStartOfPhaseEffect extends DefaultEffect {
    private final PhysicalCard _source;
    private final Filterable[] _filters;
    private final Evaluator _evaluator;
    private final Phase _phase;
    private final DefaultGame _game;

    public SnapshotAndApplyStrengthModifierUntilStartOfPhaseEffect(DefaultGame game, PhysicalCard source, Evaluator evaluator, Phase phase, Filterable... filter) {
        _source = source;
        _evaluator = evaluator;
        _phase = phase;
        _filters = filter;
        _game = game;
    }

    @Override
    public String getText() {
        return "Apply strength modifier";
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        final Collection<PhysicalCard> affectedCards = Filters.filterActive(_game, _filters);
        for (PhysicalCard physicalCard : affectedCards) {
            final int modifier = _evaluator.evaluateExpression(_game, physicalCard);
            if (modifier != 0)
                _game.getModifiersEnvironment().addUntilStartOfPhaseModifier(
                        new StrengthModifier(_source, Filters.sameCard(physicalCard), modifier), _phase);
        }

        return new FullEffectResult(true);
    }

    @Override
    public boolean isPlayableInFull() {
        return true;
    }
}
