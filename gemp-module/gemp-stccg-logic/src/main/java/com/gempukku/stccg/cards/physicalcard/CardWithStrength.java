package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.game.DefaultGame;

public interface CardWithStrength {

    Integer getIntegrity(DefaultGame cardGame);
    Integer getStrength(DefaultGame cardGame);
    Integer getCunning(DefaultGame cardGame);

}