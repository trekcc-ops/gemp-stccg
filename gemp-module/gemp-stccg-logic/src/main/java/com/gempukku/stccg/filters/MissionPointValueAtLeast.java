package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class MissionPointValueAtLeast implements CardFilter {

    private final int _points;

    public MissionPointValueAtLeast(int points) {
        _points = points; }
    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return physicalCard instanceof MissionCard missionCard &&
                missionCard.getPoints() >= _points;
    }
}