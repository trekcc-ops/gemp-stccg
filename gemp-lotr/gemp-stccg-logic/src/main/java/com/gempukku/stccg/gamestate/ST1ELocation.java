package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.cards.PhysicalMissionCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Quadrant;
import com.gempukku.stccg.common.filterable.Region;

import java.util.*;

public class ST1ELocation {
    private final Quadrant _quadrant;
    private final Region _region;
    private final String _locationName;
    private final List<PhysicalMissionCard> _missionCards;
    private final Set<PhysicalCard> _nonMissionCards;
    public ST1ELocation(PhysicalMissionCard mission) {
        _quadrant = mission.getQuadrant();
        _region = mission.getBlueprint().getRegion();
        _locationName = mission.getBlueprint().getLocation();
        _missionCards = new ArrayList<>();
        _nonMissionCards = new HashSet<>();
        addMission(mission);
    }

    public List<PhysicalMissionCard> getMissions() { return _missionCards; }
    public boolean hasMissions() { return _missionCards.size() > 0; }
    public void addMission(PhysicalMissionCard card) {
        _missionCards.add(card);
        card.setLocation(this);
    }
    public Quadrant getQuadrant() { return _quadrant; }
    public String getLocationName() { return _locationName; }
    public Region getRegion() { return _region; }
    public Set<Affiliation> getAffiliationIcons(String playerId) {
        if (_missionCards.size() < 1)
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
                if (Objects.equals(mission.getOwner(), playerId))
                    return mission;
            }
        }
        return null;
    }

    public void addNonMission(PhysicalCard card) {
        _nonMissionCards.add(card);
    }

    public void refreshSpacelineIndex(int newIndex) {
        for (PhysicalMissionCard mission : _missionCards)
            mission.setLocationZoneIndex(newIndex);
        for (PhysicalCard nonMission : _nonMissionCards)
            nonMission.setLocationZoneIndex(newIndex);
    }

    public boolean hasFacilityOwnedByPlayer(String playerId) {
        for (PhysicalCard nonMission : _nonMissionCards)
            if (nonMission.getBlueprint().getCardType() == CardType.FACILITY && nonMission.getOwner() == playerId)
                return true;
        return false;
    }
}