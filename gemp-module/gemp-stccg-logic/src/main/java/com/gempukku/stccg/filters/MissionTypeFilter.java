package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.game.DefaultGame;

public class MissionTypeFilter implements CardFilter {

    private final MissionType _missionType;

    public MissionTypeFilter(MissionType missionType) {
        _missionType = missionType;
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        // TODO does not account for dual-icon missions or missions that change type
        return physicalCard.getBlueprint().getMissionType() == _missionType;
    }
}