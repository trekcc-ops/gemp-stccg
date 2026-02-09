package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.requirement.YourTurnRequirement;

public class UsageLimitBlueprint {

    private enum LimitType {
        eachOfYourTurns
    }

    @JsonProperty("type")
    private LimitType type;

    @JsonProperty("count")
    private int count;

    public void applyLimitToActionBlueprint(ActionBlueprint actionBlueprint) {
        switch(type) {
            case eachOfYourTurns:
                actionBlueprint.addCost(new UsePerTurnLimitActionBlueprint(actionBlueprint, count));
                actionBlueprint.addRequirement(new YourTurnRequirement());
                break;
        }
    }

}