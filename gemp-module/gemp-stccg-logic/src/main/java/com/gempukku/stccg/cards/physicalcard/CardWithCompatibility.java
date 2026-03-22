package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.game.ST1EGame;

public interface CardWithCompatibility extends PhysicalCard {

    default boolean isCompatibleWith(ST1EGame stGame, CardWithCompatibility card) {
        return stGame.getRules().areCardsCompatible(stGame, this, card);
    }

}