package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.AwayTeam;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ReportableCard;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.GameTimer;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.modifiers.Modifier;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerClock;
import com.gempukku.stccg.player.PlayerOrder;
import com.gempukku.stccg.processes.GameProcess;

import java.util.*;

@JsonIgnoreProperties(value = { "performedActions", "phasesInOrder" }, allowGetters = true)
public class ST1EGameState extends GameState {
    @JsonProperty("spacelineLocations")
    final List<MissionLocation> _spacelineLocations = new ArrayList<>();
    @JsonProperty("awayTeams")
    final List<AwayTeam> _awayTeams = new ArrayList<>();
    private int _nextAttemptingUnitId = 1;

    @JsonProperty("gameLocations")
    private final Map<Integer, GameLocation> _locationIds = new HashMap<>();

    @SuppressWarnings("unused")
    @JsonCreator
    private ST1EGameState(@JsonProperty("currentPhase")
                         Phase currentPhase,
                         @JsonProperty("currentProcess")
                         GameProcess currentProcess,
                         @JsonProperty("playerClocks")
                         PlayerClock[] playerClocks,
                         @JsonProperty("playerOrder")
                         PlayerOrder playerOrder,
                         @JsonProperty("cardsInGame")
                         Map<Integer, PhysicalCard> cardsInGame,
                         @JsonProperty("actionLimits")
                         ActionLimitCollection actionLimitCollection,
                         @JsonProperty("players")
                         List<Player> players,
                         @JsonProperty("awayTeams")
                         List<AwayTeam> awayTeams,
                          @JsonProperty("spacelineLocations")
                          List<MissionLocation> spacelineLocations,
                          @JsonProperty("modifiers")
                          List<Modifier> modifiers
    ) {
        super(players, playerClocks, actionLimitCollection);
        _currentPhase = currentPhase;
        setCurrentProcess(currentProcess);
        _playerOrder = playerOrder;
        _allCards.putAll(cardsInGame);
        _nextCardId = Collections.max(_allCards.keySet()) + 1;
        for (AwayTeam awayTeam : awayTeams) {
            _awayTeams.add(awayTeam);
            if (awayTeam.getAwayTeamId() >= _nextAttemptingUnitId) {
                _nextAttemptingUnitId = awayTeam.getAwayTeamId() + 1;
            }
        }
        for (MissionLocation location : spacelineLocations) {
            _spacelineLocations.add(location);
            int locationId = location.getLocationId();
            _locationIds.put(locationId, location);
        }
        for (Modifier modifier : modifiers) {
            _modifiersLogic.addAlwaysOnModifier(modifier);
        }
    }



    public ST1EGameState(Iterable<String> playerIds, Map<String, PlayerClock> clocks)
            throws InvalidGameOperationException {
        super(playerIds, clocks);
        _currentPhase = Phase.SEED_DOORWAY;
        for (Player player : _players) {
            player.addCardGroup(Zone.CORE);
            player.addCardGroup(Zone.MISSIONS_PILE);
            player.addCardGroup(Zone.SEED_DECK);
            player.addCardGroup(Zone.POINT_AREA);
        }
    }

    public ST1EGameState(Iterable<String> playerIds, GameTimer gameTimer)
            throws InvalidGameOperationException {
        super(playerIds, gameTimer);
        _currentPhase = Phase.SEED_DOORWAY;
        for (Player player : _players) {
            player.addCardGroup(Zone.CORE);
            player.addCardGroup(Zone.MISSIONS_PILE);
            player.addCardGroup(Zone.SEED_DECK);
            player.addCardGroup(Zone.POINT_AREA);
        }
    }


    @Override
    public List<PhysicalCard> getZoneCards(Player player, Zone zone) {
        if (zone == Zone.DRAW_DECK || zone == Zone.HAND || zone == Zone.REMOVED ||
                zone == Zone.DISCARD || zone == Zone.CORE || zone == Zone.MISSIONS_PILE || zone == Zone.SEED_DECK)
            return player.getCardGroupCards(zone);
        else // This should never be accessed
            return _inPlay; // TODO - Should this just be an exception?
    }


    public void createPhysicalCards(Map<String, CardDeck> decks, ST1EGame cardGame) {
        try {
            for (Player player : getPlayers()) {
                String playerId = player.getPlayerId();
                for (Map.Entry<SubDeck, List<String>> entry : decks.get(playerId).getSubDecks().entrySet()) {
                    List<PhysicalCard> subDeck = new LinkedList<>();
                    for (String blueprintId : entry.getValue()) {
                        try {
                            PhysicalCard card = cardGame.addCardToGame(blueprintId, playerId);
                            subDeck.add(card);
                            _allCards.put(_nextCardId, card);
                            _nextCardId++;
                        } catch (CardNotFoundException e) {
                            cardGame.sendErrorMessage(e);
                        }
                    }
                    if (entry.getKey() == SubDeck.DRAW_DECK) {
                        player.setCardGroup(Zone.DRAW_DECK, subDeck);
                        for (PhysicalCard card : subDeck)
                            card.setZone(Zone.DRAW_DECK);
                    } else if (entry.getKey() == SubDeck.SEED_DECK) {
                        player.setCardGroup(Zone.SEED_DECK, subDeck);
                        for (PhysicalCard card : subDeck)
                            card.setZone(Zone.SEED_DECK);
                    } else if (entry.getKey() == SubDeck.MISSIONS) {
                        player.setCardGroup(Zone.MISSIONS_PILE, subDeck);
                        for (PhysicalCard card : subDeck)
                            card.setZone(Zone.MISSIONS_PILE);
                    }
                }
            }
        } catch(InvalidGameLogicException exp) {
            cardGame.sendErrorMessage(exp);
        }
    }

    public AwayTeam createNewAwayTeam(String playerName, MissionLocation location) {
        AwayTeam result = new AwayTeam(playerName, location, _nextAttemptingUnitId);
        _awayTeams.add(result);
        _nextAttemptingUnitId++;
        return result;
    }


    public boolean hasLocationsInQuadrant(Quadrant quadrant) {
        for (MissionLocation location : _spacelineLocations) {
            if (location.getQuadrant() == quadrant) return true;
        }
        return false;
    }

    @JsonIgnore
    public int getNextLocationId() {
        if (_locationIds.isEmpty()) {
            return 1;
        } else {
            return Collections.max(_locationIds.keySet()) + 1;
        }
    }


    public Integer indexOfLocation(String location, Quadrant quadrant) {
        for (int i = 0; i < _spacelineLocations.size(); i++) {
            if (Objects.equals(_spacelineLocations.get(i).getLocationName(), location) &&
                    _spacelineLocations.get(i).getQuadrant() == quadrant)
                return i;
        }
        return null;
    }

    public Integer firstInQuadrant(Quadrant quadrant) {
        for (int i = 0; i < _spacelineLocations.size(); i++) {
            if (_spacelineLocations.get(i).getQuadrant() == quadrant) return i;
        }
        return null;
    }
    public Integer lastInQuadrant(Quadrant quadrant) {
        for (int i = _spacelineLocations.size() - 1; i >= 0; i--) {
            if (_spacelineLocations.get(i).getQuadrant() == quadrant)
                return i;
        }
        return null;
    }

    public Integer firstInRegion(Region region, Quadrant quadrant) {
        if (quadrant == null || region == null)
            return null;
        for (int i = 0; i < _spacelineLocations.size(); i++) {
            if (_spacelineLocations.get(i).getQuadrant() == quadrant &&
                    (_spacelineLocations.get(i).getRegion() == region))
                return i;
        }
        return null;
    }

    public Integer lastInRegion(Region region, Quadrant quadrant) {
        for (int i = _spacelineLocations.size() - 1; i >= 0; i--) {
            if (_spacelineLocations.get(i).getQuadrant() == quadrant &&
                    (_spacelineLocations.get(i).getRegion() == region))
                return i;
        }
        return null;
    }

    @JsonIgnore
    public List<MissionLocation> getUnorderedMissionLocations() { return _spacelineLocations; }


    public List<AwayTeam> getAwayTeams() {
        return _awayTeams;
    }

    public void checkVictoryConditions(DefaultGame cardGame) {
            // TODO - VERY simplistic. Just a straight race to 100.
            // TODO - Does not account for possible scenario where both players go over 100 simultaneously
        for (Player player : getPlayers()) {
            int score = player.getScore();
            if (score >= 100)
                cardGame.playerWon(player.getPlayerId(), score + " points");
        }
    }

    @Override
    public List<Phase> getPhasesInOrder() {
        List<Phase> seedPhases = List.of(Phase.SEED_DOORWAY, Phase.SEED_MISSION, Phase.SEED_DILEMMA, Phase.SEED_FACILITY);
        List<Phase> turnPhases = List.of(Phase.START_OF_TURN, Phase.CARD_PLAY, Phase.EXECUTE_ORDERS, Phase.END_OF_TURN);
        Phase currentPhase = getCurrentPhase();
        if (seedPhases.contains(currentPhase))
            return seedPhases;
        if (turnPhases.contains(currentPhase))
            return turnPhases;
        return List.of(getCurrentPhase());
    }

    public void removeAwayTeamFromGame(AwayTeam awayTeam) {
        _awayTeams.remove(awayTeam);
    }

    public void addCardToListOfAllCards(PhysicalCard card) {
        _allCards.put(card.getCardId(), card);
    }

    public GameLocation getLocationById(int locationId) {
        return _locationIds.get(locationId);
    }

    public void addSpacelineLocation(int indexNumber, MissionLocation location) {
        _spacelineLocations.add(indexNumber, location);
        _locationIds.put(location.getLocationId(), location);
    }

    public void addCardToEligibleAwayTeam(ST1EGame game, ReportableCard card, MissionLocation mission) {
        // TODO - Assumes owner is the owner of away teams. Won't work for some scenarios - temporary control, captives, infiltrators, etc.
        // TODO - When there are multiple eligible away teams, there should be a player decision
        String cardOwnerName = card.getOwnerName();
        for (AwayTeam awayTeam : mission.getYourAwayTeamsOnSurface(game, cardOwnerName).toList()) {
            if (awayTeam.isCompatibleWith(game, card) && !isCardInAnAwayTeam(card)) {
                awayTeam.add(card);
            }
        }
        if (!isCardInAnAwayTeam(card)) {
            AwayTeam awayTeam = createNewAwayTeam(cardOwnerName, mission);
            awayTeam.add(card);
        }
    }

    private boolean isCardInAnAwayTeam(ReportableCard card) {
        for (AwayTeam awayTeam : _awayTeams) {
            if (awayTeam.getCards().contains(card)) {
                return true;
            }
        }
        return false;
    }

    public void removeCardFromAwayTeam(ST1EGame cardGame, ReportableCard card) {
        AwayTeam awayTeam = getAwayTeamForCard(card);
        if (awayTeam != null) {
            awayTeam.remove(cardGame, card);
        }
    }

    public AwayTeam getAwayTeamForCard(ReportableCard card) {
        for (AwayTeam awayTeam : _awayTeams) {
            if (awayTeam.getCards().contains(card)) {
                return awayTeam;
            }
        }
        return null;
    }

    public List<GameLocation> getOrderedSpacelineLocations() {
        List<GameLocation> result = new ArrayList<>();
        for (SpacelineIndex index : getSpacelineElements()) {
            if (index instanceof LocationSpacelineIndex locationIndex) {
                GameLocation location = _locationIds.get(locationIndex.getLocationId());
                result.add(location);
            }
        }
        return result;
    }

    @JsonProperty("spacelineElements")
    public List<SpacelineIndex> getSpacelineElements() {
        List<SpacelineIndex> result = new ArrayList<>();
        for (MissionLocation mission : _spacelineLocations) {
            result.add(new LocationSpacelineIndex(mission));
        }
        return result;
    }
}