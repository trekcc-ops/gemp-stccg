package com.gempukku.stccg.cards;

import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalNounCard1E;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public interface CardWithHullIntegrity extends PhysicalCard {

    void applyDamage(Integer damageAmount);
    int getHullIntegrity();
    float getShields(DefaultGame cardGame);
    float getWeapons(DefaultGame cardGame);
    void stop();
    Collection<PersonnelCard> getPersonnelAboard();
    boolean isCompatibleWith(PhysicalNounCard1E otherCard);
}