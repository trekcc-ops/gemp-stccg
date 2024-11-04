package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Quadrant;
import com.gempukku.stccg.common.filterable.Region;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ST1ELocation implements Snapshotable<ST1ELocation> {
    private final Quadrant _quadrant;
    private final Region _region;
    private final String _locationName;
    private final ST1EGame _game;
    public ST1ELocation(MissionCard mission) {
        _quadrant = mission.getQuadrant();
        _region = mission.getBlueprint().getRegion();
        _locationName = mission.getBlueprint().getLocation();
        _game = mission.getGame();
        mission.setLocation(this);
    }

    public ST1ELocation(Quadrant quadrant, Region region, String locationName, ST1EGame game) {
        _quadrant = quadrant;
        _region = region;
        _locationName = locationName;
        _game = game;
    }

    public List<MissionCard> getMissions() {
        List<MissionCard> result = new ArrayList<>();
        Collection<PhysicalCard> missions = Filters.filterActive(_game, CardType.MISSION, Filters.atLocation(this));
        for (PhysicalCard card : missions) {
            if (card instanceof MissionCard missionCard)
                result.add(missionCard);
            else _game.sendMessage("Error - card of type MISSION that is not MissionCard class type");
        }
        return result;
    }

    public Quadrant getQuadrant() { return _quadrant; }
    public String getLocationName() { return _locationName; }
    public Region getRegion() { return _region; }

    public MissionCard getMissionForPlayer(String playerId) throws InvalidGameLogicException {
        if (getMissions().size() == 1) {
            return getMissions().getFirst();
        }
        else if (getMissions().size() == 2) {
            for (MissionCard mission : getMissions()) {
                if (Objects.equals(mission.getOwnerName(), playerId))
                    return mission;
            }
        }
        throw new InvalidGameLogicException("Could not find valid mission properties for player " + playerId + " at " + _locationName);
    }

    public boolean hasFacilityOwnedByPlayer(String playerId) {
            // TODO - Is this accurately capturing "owned by" as necessary?
        Player player = _game.getPlayerFromId(playerId);
        Collection<PhysicalCard> cards = Filters.filterYourActive(player, CardType.FACILITY, Filters.atLocation(this));
        return !cards.isEmpty();
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

    @Override
    public ST1ELocation generateSnapshot(SnapshotData snapshotData) {
        return new ST1ELocation(_quadrant, _region, _locationName, _game);
    }
}