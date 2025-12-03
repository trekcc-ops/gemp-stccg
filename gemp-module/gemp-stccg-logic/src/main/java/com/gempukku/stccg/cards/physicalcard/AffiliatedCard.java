package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.Set;

public interface AffiliatedCard extends PhysicalCard {

    boolean isAffiliation(Affiliation affiliation);
    boolean isMultiAffiliation();
    Affiliation getCurrentAffiliation();
    void setCurrentAffiliation(Affiliation affiliation);
    void changeAffiliation(Affiliation affiliation) throws InvalidGameLogicException, PlayerNotFoundException;
    Set<Affiliation> getAffiliationOptions();
    String getCardLink();

    default boolean matchesAffiliationOf(PhysicalCard otherCard) {
        if (otherCard instanceof AffiliatedCard affiliatedCard) {
            return getCurrentAffiliation() == affiliatedCard.getCurrentAffiliation();
        } else {
            return false;
        }
    }
}