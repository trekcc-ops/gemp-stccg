package com.gempukku.stccg.rules.st1e;

import com.gempukku.stccg.common.filterable.Affiliation;

public interface AffiliationAttackRestrictions {

    boolean canInitiateBattle(Affiliation attacking, Affiliation defending);

}
