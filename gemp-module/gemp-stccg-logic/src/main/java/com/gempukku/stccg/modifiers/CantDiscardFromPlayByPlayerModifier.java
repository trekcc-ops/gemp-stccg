package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.game.DefaultGame;

public class CantDiscardFromPlayByPlayerModifier extends AbstractModifier {
    private final String _notPlayer;

    public CantDiscardFromPlayByPlayerModifier(PhysicalCard source, String text, Filterable affectFilter,
                                               String notPlayer) {
        super(source, text, affectFilter, ModifierEffect.DISCARD_FROM_PLAY_MODIFIER);
        _notPlayer = notPlayer;
    }

    @Override
    public boolean canBeDiscardedFromPlay(DefaultGame game, String performingPlayer, PhysicalCard card, PhysicalCard source) {
        return _notPlayer.equals(performingPlayer);
    }
}
