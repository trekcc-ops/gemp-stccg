package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.game.ST1EGame;

public interface CardWithCompatibility extends PhysicalCard {

    default boolean isCompatibleWith(ST1EGame stGame, CardWithCompatibility card) {
        if (this instanceof AffiliatedCard affil1 && card instanceof AffiliatedCard affil2 &&
                affil1.doesNotWorkWith(affil2)) {
            return false;
        } else {
            return stGame.getRules().areCardsCompatiblePerRules(this, card);
        }
    }

}