package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;

import java.util.Set;

public interface AffiliatedCard extends PhysicalCard<ST1EGame> {
    ST1EGame getGame();
    boolean isMultiAffiliation();
    Affiliation getAffiliation();
    void setCurrentAffiliation(Affiliation affiliation);
    void changeAffiliation(Affiliation affiliation) throws InvalidGameLogicException;
    Set<Affiliation> getAffiliationOptions();
    String getCardLink();

    default boolean matchesAffiliationOf(AffiliatedCard affiliatedCard) {
        return getAffiliation() == affiliatedCard.getAffiliation();
    }
}