package com.gempukku.stccg.modifiers.blueprints;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.YouCanSeedAUIconCardsModifier;

public class YouCanSeedAUCardsModifierBlueprint implements ModifierBlueprint {

    public Modifier createModifier(DefaultGame cardGame, PhysicalCard thisCard, ActionContext actionContext) {
        return new YouCanSeedAUIconCardsModifier(thisCard);
    }


}