package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.cards.PhysicalMissionCard;
import com.gempukku.stccg.cards.PhysicalFacilityCard;
import com.gempukku.stccg.common.filterable.*;

import java.util.*;

public class ST1ELocation {
    private final Quadrant _quadrant;
    private final Region _region;
    private final String _locationName;
    private final List<PhysicalMissionCard> _missionCards;
    private final Set<PhysicalCard> _nonMissionCards;
    private final Set<PhysicalFacilityCard> _outpostCards;
    public ST1ELocation(PhysicalMissionCard mission) {
        _quadrant = mission.getQuadrant();
        _region = mission.getBlueprint().getRegion();
        _locationName = mission.getBlueprint().getLocation();
        _missionCards = new ArrayList<>();
        _nonMissionCards = new HashSet<>();
        _outpostCards = new HashSet<>();
        addMission(mission);
    }

    public List<PhysicalMissionCard> getMissions() { return _missionCards; }
    public Set<PhysicalFacilityCard> getOutposts() { return _outpostCards; }
    public boolean hasMissions() { return !_missionCards.isEmpty(); }
    public void addMission(PhysicalMissionCard card) {
        _missionCards.add(card);
        card.setLocation(this);
    }
    public Quadrant getQuadrant() { return _quadrant; }
    public String getLocationName() { return _locationName; }
    public Region getRegion() { return _region; }
    public Set<Affiliation> getAffiliationIcons(String playerId) {
        if (_missionCards.isEmpty())
            return null;
        else
            // TODO - Assumes that the mission is symmetric
            return getMissionForPlayer(playerId).getBlueprint().getOwnerAffiliationIcons();
    }
    public PhysicalMissionCard getMissionForPlayer(String playerId) {
        if (_missionCards.size() == 1) {
            return _missionCards.get(0);
        }
        else if (_missionCards.size() == 2) {
            for (PhysicalMissionCard mission : _missionCards) {
                if (Objects.equals(mission.getOwnerName(), playerId))
                    return mission;
            }
        }
        return null;
    }

    public void addNonMission(PhysicalCard card) {
        _nonMissionCards.add(card);
        if (card.getBlueprint().getFacilityType() == FacilityType.OUTPOST)
            _outpostCards.add((PhysicalFacilityCard) card);
    }

    public void refreshSpacelineIndex(int newIndex) {
        for (PhysicalMissionCard mission : _missionCards)
            mission.setLocationZoneIndex(newIndex);
        for (PhysicalCard nonMission : _nonMissionCards)
            nonMission.setLocationZoneIndex(newIndex);
    }

    public boolean hasFacilityOwnedByPlayer(String playerId) {
        for (PhysicalCard nonMission : _nonMissionCards)
            if (nonMission.getBlueprint().getCardType() == CardType.FACILITY && nonMission.getOwnerName() == playerId)
                return true;
        return false;
    }
}