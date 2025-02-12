package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.player.Player;

public class YouCanPlayAUIconCardsModifier extends AbstractModifier {

    private PhysicalCard _performingCard;

    public YouCanPlayAUIconCardsModifier(PhysicalCard performingCard) {
        super(ModifierEffect.AU_CARDS_ENTER_PLAY);
    }


    public Player getAffectedPlayer() {
        return _performingCard.getController();
    }
}