package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.modifiers.YouCanPlayAUIconCardsModifier;
import com.gempukku.stccg.modifiers.YouCanSeedAUIconCardsModifier;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unused")
public class Blueprint103_032 extends CardBlueprint {

    // Alternate Universe Door
    Blueprint103_032() {
        super("103_032");
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiersFromJava(DefaultGame cardGame,
                                                                           PhysicalCard thisCard) {
        List<Modifier> result = new LinkedList<>();
        result.add(new YouCanPlayAUIconCardsModifier(thisCard));
        result.add(new YouCanSeedAUIconCardsModifier(thisCard));
        return result;
    }

}