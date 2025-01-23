package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.annotation.*;
import com.gempukku.stccg.cards.AwayTeam;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.condition.missionrequirements.MissionRequirement;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.*;
import com.google.common.collect.Iterables;

import java.util.*;
import java.util.stream.Stream;

@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="locationZoneIndex")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIncludeProperties({ "quadrant", "region", "locationName", "locationZoneIndex", "isCompleted",
        "cardsSeededUnderneath" })
public class MissionLocation {
    private final Quadrant _quadrant;
    private final Region _region;
    private final String _locationName;
    private final ST1EGame _game;
    private boolean _isCompleted;
    private final CardGroup _missionCards = new CardGroup(Zone.SPACELINE);
    protected Map<Player, List<PhysicalCard>> _cardsPreSeededUnderneath = new HashMap<>();

    private final List<PhysicalCard> _cardsSeededUnderneath = new LinkedList<>();
    public MissionLocation(MissionCard mission) {
        this(mission.getBlueprint().getQuadrant(), mission.getBlueprint().getRegion(),
                mission.getBlueprint().getLocation(), mission.getGame());
        _missionCards.addCard(mission);
        mission.setLocation(this);
    }

    public MissionLocation(Quadrant quadrant, Region region, String locationName, ST1EGame game) {
        _quadrant = quadrant;
        _region = region;
        _locationName = locationName;
        _game = game;
    }

    public Stream<AwayTeam> getYourAwayTeamsOnSurface(Player player) {
        Stream<AwayTeam> teamsOnSurface =
                _game.getGameState().getAwayTeams().stream().filter(awayTeam -> awayTeam.isOnSurface(this));
        return teamsOnSurface.filter(awayTeam -> awayTeam.getPlayer() == player);
    }

    public Stream<AwayTeam> getYourAwayTeamsOnSurface(ST1EGame game, Player player) {
        Stream<AwayTeam> teamsOnSurface =
                game.getGameState().getAwayTeams().stream().filter(awayTeam -> awayTeam.isOnSurface(this));
        return teamsOnSurface.filter(awayTeam -> awayTeam.getPlayer() == player);
    }


    public Stream<AwayTeam> getAwayTeamsOnSurface(ST1EGame cardGame) {
        return cardGame.getGameState().getAwayTeams().stream().filter(awayTeam -> awayTeam.isOnSurface(this));
    }


    public List<MissionCard> getMissionCards() {
        List<MissionCard> result = new ArrayList<>();
        Collection<PhysicalCard> missions = Filters.filterActive(_game, CardType.MISSION, Filters.atLocation(this));
        for (PhysicalCard card : missions) {
            if (card instanceof MissionCard missionCard)
                result.add(missionCard);
            else _game.sendMessage("Error - card of type MISSION that is not MissionCard class type");
        }
        return result;
    }

    public List<PhysicalCard> getMissionCardsNew() {
        return _missionCards.getCards();
    }

    public Quadrant getQuadrant() { return _quadrant; }
    public String getLocationName() { return _locationName; }
    public Region getRegion() { return _region; }

    public MissionCard getMissionForPlayer(String playerId) throws InvalidGameLogicException {
        PhysicalCard result = null;
        Collection<PhysicalCard> missionCards = getMissionCardsNew();
        if (missionCards.size() == 1) {
            result = Iterables.getOnlyElement(missionCards);
        }
        else if (missionCards.size() == 2) {
            for (PhysicalCard missionCard : missionCards) {
                if (Objects.equals(missionCard.getOwnerName(), playerId))
                    result = missionCard;
            }
        }
        if (result instanceof MissionCard missionCard)
            return missionCard;
        throw new InvalidGameLogicException("Could not find valid mission properties for player " + playerId + " at " + _locationName);
    }


    @JsonIdentityReference(alwaysAsId=true)
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = NonEmptyListFilter.class)
    public List<PhysicalCard> getCardsSeededUnderneath() { return _cardsSeededUnderneath; }

    public int getDistanceToLocation(DefaultGame cardGame, MissionLocation location, Player player)
            throws InvalidGameLogicException {
                // TODO - Not correct if you're calculating inter-quadrant distance (e.g., Bajoran Wormhole)

        ST1EGame stGame;
        if (cardGame instanceof ST1EGame)
            stGame = (ST1EGame) cardGame;
        else throw new InvalidGameLogicException("Unable to process distance between locations in this game");

        if (location.getQuadrant() != _quadrant)
            throw new InvalidGameLogicException("Tried to calculate span between quadrants");
        else {
            List<MissionLocation> spaceline = stGame.getGameState().getSpacelineLocations();
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

    public boolean mayBeAttemptedByPlayer(Player player, DefaultGame cardGame) throws InvalidGameLogicException {
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
                        awayTeam -> awayTeam.canAttemptMission(cardGame, this));
            if (missionType == MissionType.SPACE)
                return Filters.filterYourActive(cardGame, player, Filters.ship, Filters.atLocation(this))
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
        MissionCard missionCard = getMissionCards().getFirst();
        return missionCard.getBlueprint().getMissionType();
    }

    public MissionRequirement getRequirements(String playerId) throws InvalidGameLogicException {
        MissionCard card = getMissionForPlayer(playerId);
        return card.getBlueprint().getMissionRequirements();
    }

    public void complete(String completingPlayerId, DefaultGame cardGame)
            throws InvalidGameLogicException, PlayerNotFoundException {
        MissionCard missionCard = getMissionForPlayer(completingPlayerId);
        _isCompleted = true;
        cardGame.getGameState().getPlayer(completingPlayerId).scorePoints(missionCard.getPoints());
        cardGame.getGameState().checkVictoryConditions();
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
        for (MissionCard card : getMissionCards()) {
            if (card.isHomeworld())
                result = true;
        }
        return result;
    }

    public boolean isPlanet() { return getMissionType() == MissionType.PLANET || getMissionType() == MissionType.DUAL; }
    public boolean isSpace() { return getMissionType() == MissionType.SPACE || getMissionType() == MissionType.DUAL; }

    public MissionCard getTopMission() { return getMissionCards().getLast(); }

    public void addMission(MissionCard newMission) {
        newMission.stackOn(_missionCards.getCards().getFirst());
        _missionCards.addCard(newMission);
        newMission.setLocation(this);
    }
}