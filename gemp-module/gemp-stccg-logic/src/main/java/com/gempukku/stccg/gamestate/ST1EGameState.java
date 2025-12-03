package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.AwayTeam;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.GameTimer;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.game.*;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerClock;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.*;

public class ST1EGameState extends GameState {
    @JsonProperty("spacelineLocations")
    final List<MissionLocation> _spacelineLocations = new ArrayList<>();
    @JsonProperty("awayTeams")
    final List<AwayTeam> _awayTeams = new ArrayList<>();
    private int _nextAttemptingUnitId = 1;
    private int _nextLocationId = 1;

    public ST1EGameState(Iterable<String> playerIds, ST1EGame game, Map<String, PlayerClock> clocks) {
        super(game, playerIds, clocks);
        _currentPhase = Phase.SEED_DOORWAY;
        try {
            for (Player player : _players.values()) {
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
            for (Player player : _players.values()) {
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
                                    library.createST1EPhysicalCard(cardGame, blueprintId, _nextCardId, player);
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

    public AwayTeam createNewAwayTeam(Player player, MissionLocation location) {
        AwayTeam result = new AwayTeam(player.getPlayerId(), location, _nextAttemptingUnitId);
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

    public void addMissionLocationToSpaceline(MissionCard newMission, int indexNumber) {
        _spacelineLocations.add(indexNumber, new MissionLocation(newMission, _nextLocationId));
        _nextLocationId++;
        addCardToZone(newMission.getGame(), newMission, Zone.SPACELINE);
    }

    public void addMissionCardToSharedMission(MissionCard newMission, int indexNumber)
            throws InvalidGameLogicException {
        MissionLocation location = _spacelineLocations.get(indexNumber);
        List<MissionCard> missionsAtLocation = location.getMissionCards();
        if (missionsAtLocation.size() != 1 ||
                Objects.equals(missionsAtLocation.getFirst().getOwnerName(), newMission.getOwnerName()))
            throw new InvalidGameLogicException("Cannot seed " + newMission.getTitle() + " because " +
                    newMission.getOwnerName() + " already has a mission at " +
                    newMission.getBlueprint().getLocation());
        location.addMission(newMission);
        addCardToZone(newMission.getGame(), newMission, Zone.SPACELINE);
    }

    public void seedFacilityAtLocation(FacilityCard card, GameLocation location) {
        card.setLocation(location);
        addCardToZone(card.getGame(), card, Zone.AT_LOCATION);
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

    public int getSpacelineLocationsSize() { return _spacelineLocations.size(); }
    public int getQuadrantLocationsSize(Quadrant quadrant) {
        int x = 0;
        for (MissionLocation location : _spacelineLocations) {
            if (location.getQuadrant() == quadrant) x++;
        }
        return x;
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

}