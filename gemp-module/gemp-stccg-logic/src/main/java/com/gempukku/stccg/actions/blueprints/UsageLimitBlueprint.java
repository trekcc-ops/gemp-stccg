package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.requirement.YourTurnRequirement;

public class UsageLimitBlueprint {

    private enum LimitType {
        eachOfYourTurns
    }

    @JsonProperty("type")
    private final LimitType _type;

    @JsonProperty("count")
    private final int _count;

    @JsonCreator
    public UsageLimitBlueprint(@JsonProperty("type")
                                LimitType type,
                                @JsonProperty("count")
                                int count
    ) {
        _type = type;
        _count = count;
    }

    public UsageLimitBlueprint(String type, int count) {
        _type = LimitType.valueOf(type);
        _count = count;
    }

    public void applyLimitToActionBlueprint(ActionBlueprint actionBlueprint) {
        switch(_type) {
            case eachOfYourTurns:
                actionBlueprint.addCost(new UsePerTurnLimitActionBlueprint(actionBlueprint, _count));
                actionBlueprint.addRequirement(new YourTurnRequirement());
                break;
        }
    }

}