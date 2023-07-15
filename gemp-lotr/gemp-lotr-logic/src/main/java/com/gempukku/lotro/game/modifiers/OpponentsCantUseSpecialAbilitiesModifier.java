package com.gempukku.lotro.game.modifiers;

import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.game.actions.Action;

public class OpponentsCantUseSpecialAbilitiesModifier extends AbstractModifier {
    private final String _playerId;

    public OpponentsCantUseSpecialAbilitiesModifier(PhysicalCard source, String playerId) {
        this(source, null, playerId);
    }

    public OpponentsCantUseSpecialAbilitiesModifier(PhysicalCard source, Condition condition, String playerId) {
        super(source, null, null, condition, ModifierEffect.ACTION_MODIFIER);
        _playerId = playerId;
    }

    @Override
    public boolean canPlayAction(DefaultGame game, String performingPlayer, Action action) {
        if (action.getType() == Action.Type.SPECIAL_ABILITY
                && !performingPlayer.equals(_playerId))
            return false;
        return true;
    }
}