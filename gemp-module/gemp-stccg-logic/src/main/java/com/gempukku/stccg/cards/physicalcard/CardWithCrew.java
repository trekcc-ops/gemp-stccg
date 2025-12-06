package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public interface CardWithCrew extends CardWithHullIntegrity {

    Collection<PhysicalCard> getCrew(DefaultGame cardGame);

    default Collection<PersonnelCard> getPersonnelInCrew(DefaultGame cardGame) {
        Collection<PersonnelCard> personnelInCrew = new LinkedList<>();
        for (PhysicalCard card : getCrew(cardGame)) {
            if (card instanceof PersonnelCard personnel)
                personnelInCrew.add(personnel);
        }
        return personnelInCrew;
    }


    default boolean hasCardInCrew(PhysicalCard crewCard) {
        return crewCard.isAttachedTo(this) &&
                List.of(CardType.PERSONNEL, CardType.EQUIPMENT).contains(crewCard.getCardType());
    }

}