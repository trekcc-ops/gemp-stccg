package com.gempukku.stccg.cards;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;

public interface CardWithHullIntegrity extends PhysicalCard {

    void applyDamage(Integer damageAmount);

    int getHullIntegrity();

    void stop();

}