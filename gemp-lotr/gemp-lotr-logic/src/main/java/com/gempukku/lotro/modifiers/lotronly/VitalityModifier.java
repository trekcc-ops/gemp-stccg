package com.gempukku.lotro.modifiers.lotronly;

import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.modifiers.AbstractModifier;
import com.gempukku.lotro.modifiers.ModifierEffect;
import com.gempukku.lotro.evaluator.Evaluator;

public class VitalityModifier extends AbstractModifier {
    private final Evaluator _modifier;
    private final boolean _nonCardTextModifier;

    public VitalityModifier(PhysicalCard source, Filterable affectFilter, Evaluator modifier,
                            boolean nonCardTextModifier) {
        super(source, "Vitality modifier", affectFilter, ModifierEffect.VITALITY_MODIFIER);
        _modifier = modifier;
        _nonCardTextModifier = nonCardTextModifier;
    }

    @Override
    public int getVitalityModifier(DefaultGame game, PhysicalCard physicalCard) {
        return _modifier.evaluateExpression(game, physicalCard);
    }

    @Override
    public boolean isNonCardTextModifier() {
        return _nonCardTextModifier;
    }
}
