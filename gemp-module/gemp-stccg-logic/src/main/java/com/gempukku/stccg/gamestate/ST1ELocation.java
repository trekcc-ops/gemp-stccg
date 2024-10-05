package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

import java.util.*;

public class ST1ELocation {
    private final Quadrant _quadrant;
    private final Region _region;
    private final String _locationName;
    private final List<MissionCard> _missionCards;
    private final Set<PhysicalCard> _nonMissionCards;
    private final Set<FacilityCard> _outpostCards;
    private final ST1EGame _game;
    public ST1ELocation(MissionCard mission) {
        _quadrant = mission.getQuadrant();
        _region = mission.getBlueprint().getRegion();
        _locationName = mission.getBlueprint().getLocation();
        _missionCards = new ArrayList<>();
        _nonMissionCards = new HashSet<>();
        _outpostCards = new HashSet<>();
        _game = mission.getGame();
        addMission(mission);
    }

    public List<MissionCard> getMissions() { return _missionCards; }
    public Set<FacilityCard> getOutposts() { return _outpostCards; }

    public void addMission(MissionCard card) {
        _missionCards.add(card);
        card.setLocation(this);
    }
    public Quadrant getQuadrant() { return _quadrant; }
    public String getLocationName() { return _locationName; }
    public Region getRegion() { return _region; }

    public MissionCard getMissionForPlayer(String playerId) throws InvalidGameLogicException {
        if (_missionCards.size() == 1) {
            return _missionCards.getFirst();
        }
        else if (_missionCards.size() == 2) {
            for (MissionCard mission : _missionCards) {
                if (Objects.equals(mission.getOwnerName(), playerId))
                    return mission;
            }
        }
        throw new InvalidGameLogicException("Could not find valid mission properties for player " + playerId + " at " + _locationName);
    }

    public void addNonMission(PhysicalCard card) {
        _nonMissionCards.add(card);
        if (card.getBlueprint().getFacilityType() == FacilityType.OUTPOST)
            _outpostCards.add((FacilityCard) card);
    }

    public void refreshSpacelineIndex(int newIndex) {
        for (MissionCard mission : _missionCards)
            mission.setLocationZoneIndex(newIndex);
        for (PhysicalCard nonMission : _nonMissionCards)
            nonMission.setLocationZoneIndex(newIndex);
    }

    public boolean hasFacilityOwnedByPlayer(String playerId) {
        for (PhysicalCard nonMission : _nonMissionCards)
            if (nonMission.getCardType() == CardType.FACILITY && nonMission.getOwnerName().equals(playerId))
                return true;
        return false;
    }

    public int getDistanceToLocation(ST1ELocation location, Player player) throws InvalidGameLogicException {
                // TODO - Not correct if you're calculating inter-quadrant distance (e.g., Bajoran Wormhole)
        if (location.getQuadrant() != _quadrant)
            throw new InvalidGameLogicException("Tried to calculate span between quadrants");
        else {
            List<ST1ELocation> spaceline = _game.getGameState().getSpacelineLocations();
            int startingIndex = spaceline.indexOf(this);
            int endingIndex = spaceline.indexOf(location);
            int distance = 0;
            int loopStart;
            int loopEnd;
            if (startingIndex < endingIndex) {
                loopStart = startingIndex + 1;
                loopEnd = endingIndex;
            } else {
                loopStart = endingIndex;
                loopEnd = startingIndex - 1;
            }
            for (int i = loopStart; i <= loopEnd; i++) {
                distance += spaceline.get(i).getSpan(player);
            }
            return distance;
        }
    }

    public int getLocationZoneIndex() {
        return _game.getGameState().getSpacelineLocations().indexOf(this);
    }

    public int getSpan(Player player) throws InvalidGameLogicException {
        return getMissionForPlayer(player.getPlayerId()).getSpan(player);
    }

}