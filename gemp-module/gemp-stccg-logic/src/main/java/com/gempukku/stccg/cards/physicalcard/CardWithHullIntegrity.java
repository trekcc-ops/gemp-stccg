package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public interface CardWithHullIntegrity extends PhysicalCard {

    void applyDamage(Integer damageAmount);
    int getHullIntegrity();
    float getShields(DefaultGame cardGame);
    float getWeapons(DefaultGame cardGame);
    void stop();

    Collection<PersonnelCard> getPersonnelAboard(DefaultGame cardGame);
}