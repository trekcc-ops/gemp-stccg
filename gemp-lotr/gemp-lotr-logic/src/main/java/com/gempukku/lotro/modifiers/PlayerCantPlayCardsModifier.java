package com.gempukku.lotro.modifiers;

import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.actions.Action;
import com.gempukku.lotro.condition.Condition;

public class PlayerCantPlayCardsModifier extends AbstractModifier {
    private final String _playerId;

    public PlayerCantPlayCardsModifier(PhysicalCard source, String playerId) {
        this(source, null, playerId);
    }

    public PlayerCantPlayCardsModifier(PhysicalCard source, Condition condition, String playerId) {
        super(source, null, null, condition, ModifierEffect.ACTION_MODIFIER);
        _playerId = playerId;
    }

    @Override
    public boolean canPlayAction(DefaultGame game, String performingPlayer, Action action) {
        return action.getActionType() != Action.ActionType.PLAY_CARD
                || !performingPlayer.equals(_playerId);
    }
}
