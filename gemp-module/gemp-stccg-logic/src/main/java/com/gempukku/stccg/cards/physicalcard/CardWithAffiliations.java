package com.gempukku.stccg.cards.physicalcard;

import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;

public interface CardWithAffiliations {

    boolean hasAffiliation(DefaultGame cardGame, Affiliation affiliation, String requestingPlayerName);

    boolean matchesAffiliationOfCard(ST1EGame stGame, CardWithAffiliations otherCard, String requestingPlayerName);

}