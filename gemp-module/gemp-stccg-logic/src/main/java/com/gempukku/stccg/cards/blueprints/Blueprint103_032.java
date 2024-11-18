package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.modifiers.CanPlayAUIconCardsModifier;
import com.gempukku.stccg.modifiers.CanSeedAUIconCardsModifier;
import com.gempukku.stccg.modifiers.Modifier;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unused")
public class Blueprint103_032 extends CardBlueprint {

    // Alternate Universe Door
    Blueprint103_032() {
        super("103_032");
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(Player player, final PhysicalCard thisCard) {
        List<Modifier> result = new LinkedList<>();
        result.add(new CanPlayAUIconCardsModifier(thisCard.getOwner()));
        result.add(new CanSeedAUIconCardsModifier(thisCard.getOwner()));
        return result;
    }

}