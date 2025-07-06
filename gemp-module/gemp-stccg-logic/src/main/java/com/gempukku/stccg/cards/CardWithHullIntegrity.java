package com.gempukku.stccg.cards;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public interface CardWithHullIntegrity extends PhysicalCard {

    void applyDamage(Integer damageAmount);

    int getHullIntegrity();

    int getShields(DefaultGame cardGame);

    void stop();

}