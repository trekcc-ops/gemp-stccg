package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;
import java.util.Objects;

public interface CardWithHullIntegrity extends CardWithAttributes {

    void applyDamage(Integer damageAmount);
    int getHullIntegrity();
    Integer getShields(DefaultGame cardGame);
    Integer getWeapons(DefaultGame cardGame);
    Integer getRange(DefaultGame cardGame);

    default int getTotalAttributes(DefaultGame cardGame) {
        return Objects.requireNonNullElse(getRange(cardGame), 0) +
                Objects.requireNonNullElse(getWeapons(cardGame), 0) +
                Objects.requireNonNullElse(getShields(cardGame), 0);
    }

    void stop();

    Collection<PersonnelCard> getPersonnelAboard(DefaultGame cardGame);
}