package com.gempukku.stccg.rules.st1e;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.game.ST1EGame;

public class ST1EAffiliationAttackRestrictionsRule {
    private final ST1EGame _game;

    public ST1EAffiliationAttackRestrictionsRule(ST1EGame game) {
        _game = game;
    }

    public void applyRule() {
            // TODO - Convert this into an action proxy so it can extend from ST1ERule
                // TODO - This doesn't account for complexity of mixed-affiliation forces
        _game.setAffiliationAttackRestrictions(
                (attacking, defending) -> {
                    if (attacking == Affiliation.KLINGON || attacking == Affiliation.KAZON ||
                            attacking == Affiliation.NON_ALIGNED || attacking == Affiliation.NEUTRAL)
                        return true;
                    else if (attacking == Affiliation.FEDERATION)
                        return defending == Affiliation.BORG;
                    else return defending != attacking;
                }
        );
    }
}