package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Quadrant;
import com.gempukku.stccg.common.filterable.Region;

import java.util.HashSet;
import java.util.Set;

public class Location {
    private final Quadrant _quadrant;
    private final Region _region;
    private final String _locationName;
    private final Set<PhysicalCard> _missionCards;
    public Location(PhysicalCard mission) {
        _quadrant = mission.getQuadrant();
        _region = mission.getBlueprint().getRegion();
        _locationName = mission.getBlueprint().getLocation();
        _missionCards = new HashSet<>();
        _missionCards.add(mission);
    }

    public Location(String locationName, Quadrant quadrant, Region region, Set<PhysicalCard> missionCards) {
        _quadrant = quadrant;
        _region = region;
        _locationName = locationName;
        _missionCards = missionCards;
    }

    public Set<PhysicalCard> getMissions() { return _missionCards; }
    public void addMission(PhysicalCard card) { _missionCards.add(card); }
    public Quadrant getQuadrant() { return _quadrant; }
    public String getLocationName() { return _locationName; }
    public Region getRegion() { return _region; }

}