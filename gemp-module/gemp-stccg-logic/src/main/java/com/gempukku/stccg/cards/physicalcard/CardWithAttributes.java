package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.game.DefaultGame;

public interface CardWithAttributes extends PhysicalCard {

    int getTotalAttributes(DefaultGame cardGame);
}