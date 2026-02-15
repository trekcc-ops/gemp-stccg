package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public interface CardWithHullIntegrity extends PhysicalCard {

    void applyDamage(Integer damageAmount);
    int getHullIntegrity();
    Integer getShields(DefaultGame cardGame);
    Integer getWeapons(DefaultGame cardGame);
    Integer getRange(DefaultGame cardGame);

    void stop();

    Collection<PersonnelCard> getPersonnelAboard(DefaultGame cardGame);
}