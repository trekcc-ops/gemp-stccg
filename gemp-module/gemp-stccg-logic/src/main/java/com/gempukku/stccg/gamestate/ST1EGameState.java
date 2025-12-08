package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.playcard.SeedMissionCardAction;
import com.gempukku.stccg.actions.playcard.SeedOutpostAction;
import com.gempukku.stccg.cards.AwayTeam;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ReportableCard;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.GameTimer;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerClock;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.player.PlayerOrder;
import com.gempukku.stccg.processes.GameProcess;

import java.util.*;

@JsonIgnoreProperties(value = { "actions", "spacelineLocations",
        "performedActions", "phasesInOrder", "requestingPlayer", "channelNumber", "timeStamp" }, allowGetters = true)
public class ST1EGameState extends GameState {
    @JsonProperty("spacelineLocations")
    final List<MissionLocation> _spacelineLocations = new ArrayList<>();
    @JsonProperty("awayTeams")
    final List<AwayTeam> _awayTeams = new ArrayList<>();
    private int _nextAttemptingUnitId = 1;
    private int _nextLocationId = 1;
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
                         List<AwayTeam> awayTeams
    ) {
        super(players, playerClocks, actionLimitCollection);
        setCurrentPhase(currentPhase);
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
    }



    public ST1EGameState(Iterable<String> playerIds, ST1EGame game, Map<String, PlayerClock> clocks) {
        super(game, playerIds, clocks);
        _currentPhase = Phase.SEED_DOORWAY;
        try {
            for (Player player : _players) {
                player.addCardGroup(Zone.CORE);
                player.addCardGroup(Zone.MISSIONS_PILE);
                player.addCardGroup(Zone.SEED_DECK);
            }
        } catch(InvalidGameLogicException exp) {
            game.sendErrorMessage(exp);
            game.cancelGame();
        }
    }

    public ST1EGameState(Iterable<String> playerIds, ST1EGame game, GameTimer gameTimer) {
        super(game, playerIds, gameTimer);
        _currentPhase = Phase.SEED_DOORWAY;
        try {
            for (Player player : _players) {
                player.addCardGroup(Zone.CORE);
                player.addCardGroup(Zone.MISSIONS_PILE);
                player.addCardGroup(Zone.SEED_DECK);
            }
        } catch(InvalidGameLogicException exp) {
            game.sendErrorMessage(exp);
            game.cancelGame();
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


    public void createPhysicalCards(CardBlueprintLibrary library, Map<String, CardDeck> decks, ST1EGame cardGame) {
        try {
            for (Player player : getPlayers()) {
                String playerId = player.getPlayerId();
                for (Map.Entry<SubDeck, List<String>> entry : decks.get(playerId).getSubDecks().entrySet()) {
                    List<PhysicalCard> subDeck = new LinkedList<>();
                    for (String blueprintId : entry.getValue()) {
                        try {
                            PhysicalCard card =
                                    library.createST1EPhysicalCard(blueprintId, _nextCardId, player);
                            subDeck.add(card);
                            _allCards.put(_nextCardId, card);
                            _nextCardId++;
                        } catch (CardNotFoundException | PlayerNotFoundException e) {
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

    public int getAndIncrementNextLocationId() {
        int result = _nextLocationId;
        _nextLocationId++;
        return result;
    }

    public void addMissionLocationToSpacelineForTestingOnly(ST1EGame cardGame, MissionCard newMission, int indexNumber) {
        // Do not use in non-testing classes
        SeedMissionCardAction seedAction = new SeedMissionCardAction(cardGame, newMission, indexNumber);
        seedAction.seedCard(cardGame);
    }

    public void seedFacilityAtLocationForTestingOnly(ST1EGame cardGame, FacilityCard card, MissionCard destinationCard)
            throws InvalidGameLogicException, PlayerNotFoundException {
        // Do not use in non-testing classes
        SeedOutpostAction seedAction = new SeedOutpostAction(cardGame, card, destinationCard);
        seedAction.processEffect(cardGame);
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

    public List<MissionLocation> getSpacelineLocations() { return _spacelineLocations; }


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

    public boolean cardsArePresentWithEachOther(PhysicalCard... cards) {
        for (PhysicalCard card1 : cards) {
            for (PhysicalCard card2 : cards) {
                boolean presentWithEachOther = card1.isAtSameLocationAsCard(card2) &&
                            card1.getGameLocation(this) instanceof MissionLocation missionLocation &&
                            card1.getAttachedToCardId() != null &&
                            card1.getAttachedToCardId().equals(card2.getAttachedToCardId()) &&
                            _spacelineLocations.contains(missionLocation);
                if (!presentWithEachOther) {
                    return false;
                }
            }
        }
        return true;
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
}