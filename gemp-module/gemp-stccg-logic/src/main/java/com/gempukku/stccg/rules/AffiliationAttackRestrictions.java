package com.gempukku.stccg.rules;

import com.gempukku.stccg.common.filterable.Affiliation;

public interface AffiliationAttackRestrictions {

    boolean canInitiateBattle(Affiliation attacking, Affiliation defending);

}
