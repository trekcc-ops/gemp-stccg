package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.game.ST1EGame;

import java.util.Set;

public interface AffiliatedCard {
    ST1EGame getGame();
    boolean isMultiAffiliation();
    Affiliation getCurrentAffiliation();
    void setCurrentAffiliation(Affiliation affiliation);
    Set<Affiliation> getAffiliationOptions();
    String getCardLink();
}
