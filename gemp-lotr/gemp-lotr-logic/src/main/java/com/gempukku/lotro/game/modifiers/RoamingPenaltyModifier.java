package com.gempukku.lotro.game.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.DefaultGame;

public class RoamingPenaltyModifier extends AbstractModifier {
    private final int _modifier;

    public RoamingPenaltyModifier(PhysicalCard source, Filterable affectFilter, int modifier) {
        super(source, "Roaming penalty " + ((modifier > 0) ? ("+" + modifier) : modifier), affectFilter, ModifierEffect.TWILIGHT_COST_MODIFIER);
        _modifier = modifier;
    }

    public RoamingPenaltyModifier(PhysicalCard source, Filterable affectFilter, Condition condition, int modifier) {
        super(source, "Roaming penalty " + ((modifier > 0) ? ("+" + modifier) : modifier), affectFilter, condition, ModifierEffect.TWILIGHT_COST_MODIFIER);
        _modifier = modifier;
    }

    @Override
    public int getRoamingPenaltyModifier(DefaultGame game, PhysicalCard physicalCard) {
        return _modifier;
    }
}