package com.gempukku.stccg.modifiers.blueprints;

import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.YouCanSeedAUIconCardsModifier;

public class YouCanSeedAUCardsModifierBlueprint implements ModifierBlueprint {

    public Modifier createModifier(DefaultGame cardGame, PhysicalCard thisCard, GameTextContext actionContext) {
        return new YouCanSeedAUIconCardsModifier(thisCard);
    }


}