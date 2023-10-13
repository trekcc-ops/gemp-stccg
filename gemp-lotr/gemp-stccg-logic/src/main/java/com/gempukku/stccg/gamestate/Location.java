package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Quadrant;
import com.gempukku.stccg.common.filterable.Region;

import java.util.ArrayList;
import java.util.List;

public class Location {
    private final Quadrant _quadrant;
    private final Region _region;
    private final String _locationName;
    private final List<PhysicalCard> _missionCards;
    public Location(PhysicalCard mission) {
        _quadrant = mission.getQuadrant();
        _region = mission.getBlueprint().getRegion();
        _locationName = mission.getBlueprint().getLocation();
        _missionCards = new ArrayList<>();
        _missionCards.add(mission);
    }

    public List<PhysicalCard> getMissions() { return _missionCards; }
    public void addMission(PhysicalCard card) { _missionCards.add(card); }
    public Quadrant getQuadrant() { return _quadrant; }
    public String getLocationName() { return _locationName; }
    public Region getRegion() { return _region; }

}