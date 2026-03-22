package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.ComparatorType;
import com.gempukku.stccg.game.DefaultGame;

public class MissionPointValueFilter implements CardFilter {

    @JsonProperty("points")
    private final int _points;

    @JsonProperty("comparator")
    private final ComparatorType _comparatorType;

    public MissionPointValueFilter(int points, ComparatorType comparatorType) {
        _points = points;
        _comparatorType = comparatorType;
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return physicalCard instanceof MissionCard missionCard &&
                _comparatorType.isTrue(missionCard.getPoints(), _points);
    }
}