package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.Player;

public class YouCanSeedAUIconCardsModifier extends AbstractModifier {

    private final PhysicalCard _modifierSourceCard;

    public YouCanSeedAUIconCardsModifier(PhysicalCard performingCard) {
        super(ModifierEffect.AU_CARDS_ENTER_PLAY);
        _modifierSourceCard = performingCard;
    }


    public Player getAffectedPlayer() {
        return _modifierSourceCard.getController();
    }
}