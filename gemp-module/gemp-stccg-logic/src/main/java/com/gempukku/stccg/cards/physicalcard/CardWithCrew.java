package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;
import java.util.LinkedList;

public interface CardWithCrew extends CardWithCompatibility, CardWithHullIntegrity {

    Collection<PhysicalCard> getCrew(DefaultGame cardGame);

    Collection<PhysicalCard> getCardsAboard(DefaultGame cardGame);

    default Collection<PersonnelCard> getPersonnelInCrew(DefaultGame cardGame) {
        Collection<PersonnelCard> personnelInCrew = new LinkedList<>();
        for (PhysicalCard card : getCrew(cardGame)) {
            if (card instanceof PersonnelCard personnel)
                personnelInCrew.add(personnel);
        }
        return personnelInCrew;
    }
}