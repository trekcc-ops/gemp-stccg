package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.game.DefaultGame;

public interface CardWithAttributes extends PhysicalCard {

    int getTotalAttributes(DefaultGame cardGame);

    default int getAttribute(DefaultGame cardGame, CardAttribute attribute) {
        return cardGame.getAttribute(this, attribute);
    }
}