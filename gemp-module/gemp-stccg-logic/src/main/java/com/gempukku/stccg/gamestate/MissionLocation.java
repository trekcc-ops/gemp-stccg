package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gempukku.stccg.cards.AwayTeam;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.condition.missionrequirements.MissionRequirement;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.*;

import java.util.*;
import java.util.stream.Stream;

@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="locationZoneIndex")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIncludeProperties({ "quadrant", "region", "locationName", "locationZoneIndex", "isCompleted",
        "cardsSeededUnderneath" })
public class MissionLocation implements Snapshotable<MissionLocation> {
    private final Quadrant _quadrant;
    private final Region _region;
    private final String _locationName;
    private final ST1EGame _game;
    private boolean _isCompleted;
    protected Map<Player, List<PhysicalCard>> _cardsPreSeededUnderneath = new HashMap<>();

    private final List<PhysicalCard> _cardsSeededUnderneath = new LinkedList<>();
    public MissionLocation(MissionCard mission) {
        this(mission.getBlueprint().getQuadrant(), mission.getBlueprint().getRegion(),
                mission.getBlueprint().getLocation(), mission.getGame());
        mission.setLocation(this);
    }

    public MissionLocation(Quadrant quadrant, Region region, String locationName, ST1EGame game) {
        _quadrant = quadrant;
        _region = region;
        _locationName = locationName;
        _game = game;
    }

    public Stream<AwayTeam> getYourAwayTeamsOnSurface(Player player) {
        return getAwayTeamsOnSurface().filter(awayTeam -> awayTeam.getPlayer() == player);
    }
    public Stream<AwayTeam> getAwayTeamsOnSurface() {
        return _game.getGameState().getAwayTeams().stream().filter(awayTeam -> awayTeam.isOnSurface(this));
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
        GameState gameState = _game.getGameState();
        Player player = gameState.getPlayer(playerId);
        Collection<PhysicalCard> cards = Filters.filterYourActive(player, CardType.FACILITY, Filters.atLocation(this));
        return !cards.isEmpty();
    }

    @JsonIdentityReference(alwaysAsId=true)
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NonEmptyListFilter.class)
    public List<PhysicalCard> getCardsSeededUnderneath() { return _cardsSeededUnderneath; }

    public int getDistanceToLocation(MissionLocation location, Player player) throws InvalidGameLogicException {
                // TODO - Not correct if you're calculating inter-quadrant distance (e.g., Bajoran Wormhole)
        if (location.getQuadrant() != _quadrant)
            throw new InvalidGameLogicException("Tried to calculate span between quadrants");
        else {
            List<MissionLocation> spaceline = _game.getGameState().getSpacelineLocations();
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

    private int getSpan(Player player) throws InvalidGameLogicException {
        MissionCard card = getMissionForPlayer(player.getPlayerId());
        if (card.getOwner() == player)
            return card.getBlueprint().getOwnerSpan();
        else return card.getBlueprint().getOpponentSpan();
    }

    @Override
    public MissionLocation generateSnapshot(SnapshotData snapshotData) {
        return new MissionLocation(_quadrant, _region, _locationName, _game);
    }

    public boolean mayBeAttemptedByPlayer(Player player) throws InvalidGameLogicException {
        // Rule 7.2.1, Paragraph 1
        // TODO - Does not address shared missions, multiple copies of universal missions, or dual missions
        MissionCard missionCard = getMissionForPlayer(player.getPlayerId());
        MissionType missionType = missionCard.getBlueprint().getMissionType();
        if (missionCard.getBlueprint().hasNoPointBox())
            return false;
        if (_isCompleted)
            return false;
        if (missionCard.wasSeededBy(player) || missionCard.getPointsShown() >= 40) {
            if (missionType == MissionType.PLANET)
                return getYourAwayTeamsOnSurface(player).anyMatch(
                        awayTeam -> awayTeam.canAttemptMission(this));
            if (missionType == MissionType.SPACE)
                return Filters.filterYourActive(player, Filters.ship, Filters.atLocation(this))
                        .stream().anyMatch(ship -> ((PhysicalShipCard) ship).canAttemptMission(this));
        }
        return false;
    }

    public Set<Affiliation> getAffiliationIcons(String playerId) throws InvalidGameLogicException {
        MissionCard card = getMissionForPlayer(playerId);
        CardBlueprint blueprint = card.getBlueprint();
        if (Objects.equals(playerId, card.getOwnerName())) {
            return blueprint.getOwnerAffiliationIcons();
        } else if (blueprint.getOpponentAffiliationIcons() == null) {
            return blueprint.getOwnerAffiliationIcons();
        } else {
            return blueprint.getOwnerAffiliationIcons();
        }
    }

    public Set<Affiliation> getAffiliationIconsForPlayer(Player player) throws InvalidGameLogicException {
        return getAffiliationIcons(player.getPlayerId());
    }

    public MissionType getMissionType() {
        MissionCard missionCard = getMissions().getFirst();
        return missionCard.getBlueprint().getMissionType();
    }

    public MissionRequirement getRequirements(String playerId) throws InvalidGameLogicException {
        MissionCard card = getMissionForPlayer(playerId);
        return card.getBlueprint().getMissionRequirements();
    }

    public void complete(String completingPlayerId) throws InvalidGameLogicException {
        MissionCard missionCard = getMissionForPlayer(completingPlayerId);
        _isCompleted = true;
        _game.getGameState().getPlayer(completingPlayerId).scorePoints(missionCard.getPoints());
        _game.getGameState().checkVictoryConditions();
    }

    public void removeSeedCard(PhysicalCard cardToRemove) {
        _cardsSeededUnderneath.remove(cardToRemove);
    }
    public Collection<PhysicalCard> getCardsPreSeeded(Player player) {
        if (_cardsPreSeededUnderneath.get(player) == null)
            return new LinkedList<>();
        else return _cardsPreSeededUnderneath.get(player);
    }

    public void seedPreSeeds() {
        // TODO - This won't work quite right for shared missions
        Set<Player> playersWithSeeds = _cardsPreSeededUnderneath.keySet();
        for (Player player : playersWithSeeds) {
            for (PhysicalCard card : _cardsPreSeededUnderneath.get(player)) {
                _cardsSeededUnderneath.add(card);
                card.setLocation(this);
            }
            _cardsPreSeededUnderneath.remove(player);
        }
    }

    public void addCardToPreSeeds(PhysicalCard card, Player player) {
        _cardsPreSeededUnderneath.computeIfAbsent(player, k -> new LinkedList<>());
        _cardsPreSeededUnderneath.get(player).add(card);
    }

    public void removePreSeedCard(PhysicalCard card, Player player) {
        _cardsPreSeededUnderneath.get(player).remove(card);
    }

    public boolean isCompleted() {
        return _isCompleted;
    }

    public void addCardToSeededUnder(PhysicalCard card) {
        _cardsSeededUnderneath.add(card);
    }

    public void setCompleted(boolean completed) {
        _isCompleted = completed;
    }

    public boolean isHomeworld() {
        boolean result = false;
        for (MissionCard card : getMissions()) {
            if (card.isHomeworld())
                result = true;
        }
        return result;
    }

    public boolean isPlanet() { return getMissionType() == MissionType.PLANET || getMissionType() == MissionType.DUAL; }
    public boolean isSpace() { return getMissionType() == MissionType.SPACE || getMissionType() == MissionType.DUAL; }

    public MissionCard getTopMission() { return getMissions().getLast(); }

}