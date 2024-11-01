package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.cards.AwayTeam;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.game.*;

import java.util.*;

public class ST1EGameState extends GameState implements Snapshotable<ST1EGameState> {
    private final Map<String, List<PhysicalCard>> _seedDecks = new HashMap<>();
    private final Map<String, List<PhysicalCard>> _missionPiles = new HashMap<>();
    private final List<ST1ELocation> _spacelineLocations = new ArrayList<>();
    private final ST1EGame _game;
    private final Set<AwayTeam> _awayTeams = new HashSet<>();

    public ST1EGameState(Iterable<String> playerIds, ST1EGame game) {
        super(playerIds);
        _game = game;
        _currentPhase = Phase.SEED_DOORWAY;
        _cardGroups.put(Zone.TABLE, new HashMap<>());
        for (String playerId : playerIds)
            _cardGroups.get(Zone.TABLE).put(playerId, new LinkedList<>());
    }

    @Override
    public ST1EGame getGame() { return _game; }

    @Override
    public List<PhysicalCard> getZoneCards(String playerId, Zone zone) {
        if (zone == Zone.DRAW_DECK || zone == Zone.HAND || zone == Zone.REMOVED ||
                zone == Zone.DISCARD || zone == Zone.TABLE)
            return _cardGroups.get(zone).get(playerId);
        else if (zone == Zone.STACKED)
            return _stacked.get(playerId);
        else if (zone == Zone.MISSIONS_PILE)
            return _missionPiles.get(playerId);
        else if (zone == Zone.SEED_DECK)
            return _seedDecks.get(playerId);
        else // This should never be accessed
            return _inPlay; // TODO - Should this just be an exception?
    }

    public void createPhysicalCards(CardBlueprintLibrary library, Map<String, CardDeck> decks) {
        for (Player player : getPlayers()) {
            String playerId = player.getPlayerId();
            for (Map.Entry<SubDeck,List<String>> entry : decks.get(playerId).getSubDecks().entrySet()) {
                List<PhysicalCard> subDeck = new LinkedList<>();
                for (String blueprintId : entry.getValue()) {
                    try {
                        PhysicalCard card = library.createPhysicalCard(_game, blueprintId, _nextCardId, playerId);
                        subDeck.add(card);
                        _allCards.put(_nextCardId, card);
                        _nextCardId++;
                    } catch (CardNotFoundException e) {
                        throw new RuntimeException("Card blueprint not found");
                    }
                }
                if (entry.getKey() == SubDeck.DRAW_DECK) {
                    _cardGroups.get(Zone.DRAW_DECK).put(playerId, subDeck);
                    subDeck.forEach(card -> card.setZone(Zone.DRAW_DECK));
                } else if (entry.getKey() == SubDeck.SEED_DECK) {
                    _seedDecks.put(playerId, subDeck);
                    subDeck.forEach(card -> card.setZone(Zone.SEED_DECK));
                } else if (entry.getKey() == SubDeck.MISSIONS) {
                    _missionPiles.put(playerId, subDeck);
                    subDeck.forEach(card -> card.setZone(Zone.MISSIONS_PILE));
                }
            }
            _seedDecks.computeIfAbsent(playerId, k -> new LinkedList<>());
            _missionPiles.computeIfAbsent(playerId, k -> new LinkedList<>());
        }
    }

    public List<PhysicalCard> getMissionPile(String playerId) {
        return Collections.unmodifiableList(_missionPiles.get(playerId));
    }

    public List<PhysicalCard> getSeedDeck(String playerId) {
        return Collections.unmodifiableList(_seedDecks.get(playerId));
    }

    public boolean hasLocationsInQuadrant(Quadrant quadrant) {
        for (ST1ELocation location : _spacelineLocations) {
            if (location.getQuadrant() == quadrant) return true;
        }
        return false;
    }

    public void addMissionLocationToSpaceline(MissionCard newMission, int indexNumber) {
        _spacelineLocations.add(indexNumber, new ST1ELocation(newMission));
        addCardToZone(newMission, Zone.SPACELINE, true, false);
    }

    public void addMissionCardToSharedMission(MissionCard newMission, int indexNumber)
            throws InvalidGameLogicException {
        ST1ELocation location = _spacelineLocations.get(indexNumber);
        List<MissionCard> missionsAtLocation = location.getMissions();
        if (missionsAtLocation.size() != 1 ||
                Objects.equals(missionsAtLocation.getFirst().getOwnerName(), newMission.getOwnerName()))
            throw new InvalidGameLogicException("Cannot seed " + newMission.getTitle() + " because " +
                    newMission.getOwnerName() + " already has a mission at " +
                    newMission.getBlueprint().getLocation());
        newMission.stackOn(location.getMissions().getFirst());
        location.addMission(newMission);
        addCardToZone(newMission, Zone.SPACELINE, true, true);
    }

    public void seedFacilityAtLocation(FacilityCard card, int spacelineIndex) {
        _spacelineLocations.get(spacelineIndex).addNonMission(card);
        card.setLocation(getSpacelineLocations().get(spacelineIndex));
        addCardToZone(card, Zone.AT_LOCATION, true);
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
        for (ST1ELocation location : _spacelineLocations) {
            if (location.getQuadrant() == quadrant) x++;
        }
        return x;
    }
    public List<ST1ELocation> getSpacelineLocations() { return _spacelineLocations; }

    public Set<PhysicalCard> getQuadrantLocationCards(Quadrant quadrant) {
        Set<PhysicalCard> newCollection = new HashSet<>();
        for (ST1ELocation location : _spacelineLocations)
            for (PhysicalCard mission : location.getMissions())
                if (mission.getQuadrant() == quadrant)
                    newCollection.add(mission);
        return newCollection;
    }

    @Override
    protected void sendCardsToClient(String playerId, GameStateListener listener, boolean restoreSnapshot) {
        boolean sharedMission;
        Set<PhysicalCard> cardsLeftToSend = new LinkedHashSet<>(_inPlay);
        Set<PhysicalCard> sentCardsFromPlay = new HashSet<>();

        // Send missions in order
        for (ST1ELocation location : _spacelineLocations) {
            for (int i = 0; i < location.getMissions().size(); i++) {
                sharedMission = i != 0;
                // TODO SNAPSHOT - Pretty sure this sendCreatedCardToListener function won't work with snapshotting
                PhysicalCard mission = location.getMissions().get(i);
                sendCreatedCardToListener(mission, sharedMission, listener, !restoreSnapshot);
                cardsLeftToSend.remove(mission);
                sentCardsFromPlay.add(mission);
            }
        }

        int cardsToSendAtLoopStart;
        do {
            cardsToSendAtLoopStart = cardsLeftToSend.size();
            Iterator<PhysicalCard> cardIterator = cardsLeftToSend.iterator();
            while (cardIterator.hasNext()) {
                PhysicalCard physicalCard = cardIterator.next();
                PhysicalCard attachedTo = physicalCard.getAttachedTo();
                if (attachedTo == null || sentCardsFromPlay.contains(attachedTo)) {
                    sendCreatedCardToListener(physicalCard, false, listener, !restoreSnapshot);
                    sentCardsFromPlay.add(physicalCard);

                    cardIterator.remove();
                }
            }
        } while (cardsToSendAtLoopStart != cardsLeftToSend.size() && !cardsLeftToSend.isEmpty());

        for (PhysicalCard physicalCard : _cardGroups.get(Zone.HAND).get(playerId)) {
            sendCreatedCardToListener(physicalCard, false, listener, !restoreSnapshot);
        }

        List<PhysicalCard> missionPile = _missionPiles.get(playerId);
        if (missionPile != null) {
            for (PhysicalCard physicalCard : missionPile) {
                sendCreatedCardToListener(physicalCard, false, listener, !restoreSnapshot);
            }
        }

        for (PhysicalCard physicalCard : _cardGroups.get(Zone.DISCARD).get(playerId)) {
            sendCreatedCardToListener(physicalCard, false, listener, !restoreSnapshot);
        }
    }

    public Set<AwayTeam> getAwayTeams() { return _awayTeams; }
    public void addAwayTeamToGame(AwayTeam awayTeam) { _awayTeams.add(awayTeam); }

    public void checkVictoryConditions() {
            // TODO - VERY simplistic. Just a straight race to 100.
        for (Player player : getPlayers()) {
            if (player.getScore() >= 100)
                _game.playerWon(player.getPlayerId(), player.getScore() + " points");
        }
    }

    public void sendUpdatedCardImageToClient(PhysicalCard card) {
        for (GameStateListener listener : getAllGameStateListeners())
            listener.sendEvent(new GameEvent(GameEvent.Type.UPDATE_CARD_IMAGE, card));
    }

    public void removeAwayTeamFromGame(AwayTeam awayTeam) {
        _awayTeams.remove(awayTeam);
    }

    public void seedCardsUnder(Collection<PhysicalCard> cards, PhysicalCard topCard) {
        // TODO - This probably doesn't pay close enough attention to order
        for (PhysicalCard card : cards) {
            removeCardFromZone(card);
            addCardToZone(card, Zone.VOID);
            topCard.addCardToSeededUnder(card);
        }
    }

    public void preSeedCardsUnder(Collection<PhysicalCard> cards, PhysicalCard topCard, Player player) {
        // TODO - This probably doesn't pay close enough attention to order
        for (PhysicalCard card : cards) {
            removeCardFromZone(card);
            addCardToZone(card, Zone.VOID);
            topCard.addCardToPreSeeds(card, player);
        }
    }

    @Override
    public void generateSnapshot(ST1EGameState selfSnapshot, SnapshotData snapshotData) {
        ST1EGameState snapshot = selfSnapshot;


        snapshot._playerOrder = _playerOrder;
        snapshot._currentPhase = _currentPhase;
        snapshot._playerDecisions.putAll(_playerDecisions);
        snapshot._lastMessages.addAll(_lastMessages);
        snapshot._nextCardId = _nextCardId;
        snapshot._turnNumbers.putAll(_turnNumbers);

        for (Zone zone : _cardGroups.keySet())
            copyCardGroup(_cardGroups.get(zone), snapshot._cardGroups.get(zone), snapshotData);

        // _spacelineLocations
        // _awayTeams
        copyCardGroup(_stacked, snapshot._stacked, snapshotData);
        copyCardGroup(_seedDecks, snapshot._seedDecks, snapshotData);
        copyCardGroup(_missionPiles, snapshot._missionPiles, snapshotData);
        for (PhysicalCard card : _inPlay) {
            snapshot._inPlay.add(snapshotData.getDataForSnapshot(card));
        }
        for (Integer cardId : _allCards.keySet()) {
            PhysicalCard card = _allCards.get(cardId);
            snapshot._allCards.put(cardId, snapshotData.getDataForSnapshot(card));
        }
    }

    private static void copyCardGroup(Map<String, List<PhysicalCard>> copyFrom,
                                      Map<? super String, ? super List<PhysicalCard>> copyTo,
                                      SnapshotData snapshotData) {
        for (Map.Entry<String, List<PhysicalCard>> entry : copyFrom.entrySet()) {
            List<PhysicalCard> snapshotList = new LinkedList<>();
            copyTo.put(entry.getKey(), snapshotList);
            for (PhysicalCard card : entry.getValue()) {
                snapshotList.add(snapshotData.getDataForSnapshot(card));
            }
        }
    }
}