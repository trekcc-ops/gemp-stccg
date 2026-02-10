package com.gempukku.stccg.actions.blueprints;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.requirement.PhaseRequirement;
import com.gempukku.stccg.requirement.YourTurnRequirement;

public class UsageLimitBlueprint {

    public enum LimitType {
        eachOfYourTurns, perGame
    }

    @JsonProperty("type")
    private final LimitType _type;

    @JsonProperty("count")
    private final int _count;

    @JsonProperty("inPlaceOfNormalCardPlay")
    private final boolean _inPlaceOfNormalCardPlay;

    @JsonCreator
    public UsageLimitBlueprint(@JsonProperty("type")
                                LimitType type,
                                @JsonProperty("count")
                                int count,
                               @JsonProperty("inPlaceOfNormalCardPlay")
                               boolean inPlaceOfNormalCardPlay
    ) {
        _type = type;
        _count = count;
        _inPlaceOfNormalCardPlay = inPlaceOfNormalCardPlay;
    }

    public UsageLimitBlueprint(String type, int count) {
        _type = LimitType.valueOf(type);
        _count = count;
        _inPlaceOfNormalCardPlay = false;
    }

    public void applyLimitToActionBlueprint(ActionBlueprint actionBlueprint) {
        switch(_type) {
            case eachOfYourTurns:
                actionBlueprint.addCost(new UsePerTurnLimitActionBlueprint(actionBlueprint, _count));
                actionBlueprint.addRequirement(new YourTurnRequirement());
                break;
            case perGame:
                actionBlueprint.addCost(new UsePerGameLimitActionBlueprint(actionBlueprint, _count));
        }

        if (_inPlaceOfNormalCardPlay) {
            actionBlueprint.addRequirement(new PhaseRequirement(Phase.CARD_PLAY));
            actionBlueprint.addRequirement(new YourTurnRequirement());
            actionBlueprint.addCost(new UseNormalCardPlayBlueprint());
        }
    }

}