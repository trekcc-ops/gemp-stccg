package com.gempukku.lotro.game.modifiers;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.game.actions.Action;

public class PlayersCantUsePhaseSpecialAbilitiesModifier extends AbstractModifier {
    private final Phase _phase;

    public PlayersCantUsePhaseSpecialAbilitiesModifier(PhysicalCard source, Phase phase) {
        this(source, null, phase);
    }

    public PlayersCantUsePhaseSpecialAbilitiesModifier(PhysicalCard source, Condition condition, Phase phase) {
        super(source, null, null, condition, ModifierEffect.ACTION_MODIFIER);
        _phase = phase;
    }

    @Override
    public boolean canPlayAction(DefaultGame game, String performingPlayer, Action action) {
        if (action.getType() == Action.Type.SPECIAL_ABILITY
                && action.getActionTimeword() == _phase && action.getType() == Action.Type.SPECIAL_ABILITY)
            return false;
        return true;
    }
}