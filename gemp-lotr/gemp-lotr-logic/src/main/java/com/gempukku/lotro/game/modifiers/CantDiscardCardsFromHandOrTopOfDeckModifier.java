package com.gempukku.lotro.game.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.DefaultGame;

public class CantDiscardCardsFromHandOrTopOfDeckModifier extends AbstractModifier {
    private final String _playerId;

    public CantDiscardCardsFromHandOrTopOfDeckModifier(PhysicalCard source, Condition condition, String playerId, Filterable... discardSourceAffected) {
        super(source, null, Filters.and(discardSourceAffected), condition, ModifierEffect.DISCARD_NOT_FROM_PLAY);
        _playerId = playerId;
    }

    @Override
    public boolean canDiscardCardsFromHand(DefaultGame game, String playerId, PhysicalCard source) {
        if (playerId.equals(_playerId))
            return false;
        return true;
    }

    @Override
    public boolean canDiscardCardsFromTopOfDeck(DefaultGame game, String playerId, PhysicalCard source) {
        if (playerId.equals(_playerId))
            return false;
        return true;
    }
}