package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.requirement.Condition;

public class CantDiscardCardsFromHandOrTopOfDeckModifier extends AbstractModifier {
    private final String _playerId;

    public CantDiscardCardsFromHandOrTopOfDeckModifier(PhysicalCard source, Condition condition, String playerId,
                                                       Filterable... discardSourceAffected) {
        super(source, null, Filters.and(discardSourceAffected), condition, ModifierEffect.DISCARD_NOT_FROM_PLAY);
        _playerId = playerId;
    }

    @Override
    public boolean canDiscardCardsFromHand(DefaultGame game, String playerId, PhysicalCard source) {
        return !playerId.equals(_playerId);
    }

    @Override
    public boolean canDiscardCardsFromTopOfDeck(DefaultGame game, String playerId, PhysicalCard source) {
        return !playerId.equals(_playerId);
    }
}