package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.cards.AwayTeam;
import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardDeck;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;

import java.util.*;

public class ST1EGameState extends GameState {
    private final Map<String, List<PhysicalCard>> _seedDecks;
    private final Map<String, List<PhysicalCard>> _missionPiles;
    private final List<ST1ELocation> _spacelineLocations = new ArrayList<>();
    private final ST1EGame _game;
    private final Set<AwayTeam> _awayTeams = new HashSet<>();

    public ST1EGameState(Set<String> players, Map<String, CardDeck> decks, CardBlueprintLibrary library,
                         GameFormat format, ST1EGame game) {
        super(players, decks, library, format, game);
        _game = game;
        _cardGroups.put(Zone.TABLE, new HashMap<>());
        for (String playerId : players) {
            _cardGroups.get(Zone.TABLE).put(playerId, new LinkedList<>());
        }

        _seedDecks = new HashMap<>();
        _missionPiles = new HashMap<>();
        _currentPhase = Phase.SEED_DOORWAY;
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
            return _inPlay;
    }

    @Override
    public void createPhysicalCards() {
        for (Player player : _players.values()) {
            String playerId = player.getPlayerId();
            for (Map.Entry<SubDeck,List<String>> entry : _decks.get(playerId).getSubDecks().entrySet()) {
                List<PhysicalCard> subDeck = new LinkedList<>();
                for (String blueprintId : entry.getValue()) {
                    try {
                        PhysicalCard card = _library.createPhysicalCard(_game, blueprintId, _nextCardId, playerId);
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

    public void addToSpaceline(MissionCard missionCard, int indexNumber, boolean shared) {
        GameEvent.Type eventType;
        if (shared) {
            eventType = GameEvent.Type.PUT_SHARED_MISSION_INTO_PLAY;
            assert _spacelineLocations.get(indexNumber).getMissions().size() == 1;
            missionCard.stackOn(_spacelineLocations.get(indexNumber).getMissions().iterator().next());
            _spacelineLocations.get(indexNumber).addMission(missionCard);
        } else {
            eventType = GameEvent.Type.PUT_CARD_INTO_PLAY;
            _spacelineLocations.add(indexNumber, new ST1ELocation(missionCard));
        }
        refreshSpacelineIndices();
        addCardToZone(missionCard, Zone.SPACELINE, true, eventType);
    }

    public void seedFacilityAtLocation(FacilityCard card, int spacelineIndex) {
        _spacelineLocations.get(spacelineIndex).addNonMission(card);
        card.setLocation(getSpacelineLocations().get(spacelineIndex));
        addCardToZone(card, Zone.AT_LOCATION, true, GameEvent.Type.PUT_CARD_INTO_PLAY);
    }

    public void refreshSpacelineIndices() {
        for (int i = 0; i < _spacelineLocations.size(); i++) {
            _spacelineLocations.get(i).refreshSpacelineIndex(i);
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
    public void sendGameStateToClient(String playerId, GameStateListener listener, boolean restoreSnapshot) {
        if (_playerOrder != null) {
            listener.initializeBoard(_playerOrder.getAllPlayers(), _format.discardPileIsPublic());
            if (_currentPlayerId != null)
                listener.setCurrentPlayerId(_currentPlayerId);
            if (_currentPhase != null)
                listener.setCurrentPhase(getPhaseString());

            Set<PhysicalCard> cardsLeftToSend = new LinkedHashSet<>(_inPlay);
            Set<PhysicalCard> sentCardsFromPlay = new HashSet<>();

            // Send missions in order
            for (ST1ELocation location : _spacelineLocations) {
                for (int i = 0; i < location.getMissions().size(); i++) {
                    GameEvent.Type eventType;
                    if (i == 0) {
                        eventType = restoreSnapshot ? GameEvent.Type.PUT_CARD_INTO_PLAY_WITHOUT_ANIMATING :
                                GameEvent.Type.PUT_CARD_INTO_PLAY;
                    } else {
                        eventType = GameEvent.Type.PUT_SHARED_MISSION_INTO_PLAY;
                    }
                            // TODO SNAPSHOT - Pretty sure this cardCreated function won't work with snapshotting
                    PhysicalCard mission = location.getMissions().get(i);
                    listener.cardCreated(mission, eventType);
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
                        listener.putCardIntoPlay(physicalCard, restoreSnapshot);
                        sentCardsFromPlay.add(physicalCard);

                        cardIterator.remove();
                    }
                }
            } while (cardsToSendAtLoopStart != cardsLeftToSend.size() && !cardsLeftToSend.isEmpty());

            for (PhysicalCard physicalCard : _cardGroups.get(Zone.HAND).get(playerId)) {
                listener.putCardIntoPlay(physicalCard, restoreSnapshot);
            }

            List<PhysicalCard> missionPile = _missionPiles.get(playerId);
            if (missionPile != null) {
                for (PhysicalCard physicalCard : missionPile) {
                    listener.putCardIntoPlay(physicalCard, restoreSnapshot);
                }
            }

            for (PhysicalCard physicalCard : _cardGroups.get(Zone.DISCARD).get(playerId)) {
                listener.putCardIntoPlay(physicalCard, restoreSnapshot);
            }

            listener.sendGameStats(getGame().getTurnProcedure().getGameStats());
        }

        for (String lastMessage : _lastMessages)
            listener.sendMessage(lastMessage);

        final AwaitingDecision awaitingDecision = _playerDecisions.get(playerId);
        if (awaitingDecision != null)
            listener.decisionRequired(playerId, awaitingDecision);
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
            listener.cardImageUpdated(card);
    }

    public void removeAwayTeamFromGame(AwayTeam awayTeam) {
        _awayTeams.remove(awayTeam);
    }
}