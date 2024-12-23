package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.Player;

public class YouCanPlayAUIconCardsModifier extends AbstractModifier {

    private PhysicalCard _performingCard;

    public YouCanPlayAUIconCardsModifier(PhysicalCard performingCard) {
        super(performingCard.getGame(), ModifierEffect.AU_CARDS_ENTER_PLAY);
    }


    public Player getAffectedPlayer() {
        return _performingCard.getController();
    }
}