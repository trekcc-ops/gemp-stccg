package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public class YouCanSeedAUIconCardsModifier extends AbstractModifier {

    public YouCanSeedAUIconCardsModifier(PhysicalCard performingCard) {
        super(performingCard, ModifierEffect.AU_CARDS_ENTER_PLAY);
    }


}