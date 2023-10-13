package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardDeck;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.game.ST1EGame;

import java.util.*;

public class ST1EGameState extends GameState {
    private final Map<String, List<PhysicalCard>> _seedDecks;
    private final Map<String, List<PhysicalCard>> _missionPiles;
    private final List<Location> _spacelineLocations = new ArrayList<>();
    private final Map<String, List<PhysicalCard>> _tableCards;
    private final ST1EGame _game;

    public ST1EGameState(Set<String> players, Map<String, CardDeck> decks, CardBlueprintLibrary library, GameFormat format, ST1EGame game) {
        super(players, decks, library, format);
        _format = format;
        _game = game;
        _seedDecks = new HashMap<>();
        _missionPiles = new HashMap<>();
        _tableCards = new HashMap<>();
        for (String player : players) {
            _tableCards.put(player, new LinkedList<>());
        }
        _currentPhase = Phase.SEED_DOORWAY;
    }

    @Override
    public List<PhysicalCard> getZoneCards(String playerId, Zone zone) {
        if (zone == Zone.DRAW_DECK)
            return this._drawDecks.get(playerId);
        else if (zone == Zone.DISCARD)
            return _discards.get(playerId);
        else if (zone == Zone.HAND)
            return _hands.get(playerId);
        else if (zone == Zone.REMOVED)
            return _removed.get(playerId);
        else if (zone == Zone.STACKED)
            return _stacked.get(playerId);
        else if (zone == Zone.MISSIONS_PILE)
            return _missionPiles.get(playerId);
        else if (zone == Zone.TABLE)
            return _tableCards.get(playerId);
        else // This should never be accessed
            return _inPlay;
    }

    @Override
    public void createPhysicalCards() {
        for (String playerId : _players.keySet()) {
            for (Map.Entry<SubDeck,List<String>> entry : _decks.get(playerId).getSubDecksWithEnum().entrySet()) {
                List<PhysicalCard> subDeck = new LinkedList<>();
                for (String blueprintId : entry.getValue()) {
                    try {
                        subDeck.add(new PhysicalCard(_nextCardId, blueprintId, playerId, _library.getCardBlueprint(blueprintId)));
                        _nextCardId++;
                    } catch (CardNotFoundException e) {
                        throw new RuntimeException("Card blueprint not found");
                    }
                }
                if (entry.getKey() == SubDeck.DRAW_DECK) {
                    _drawDecks.put(playerId, subDeck);
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
        for (Location location : _spacelineLocations) {
            if (location.getQuadrant() == quadrant) return true;
        }
        return false;
    }

    public void addToSpaceline(PhysicalCard missionCard, int indexNumber, boolean shared) {
        GameEvent.Type eventType;
        if (shared) {
            eventType = GameEvent.Type.PUT_SHARED_MISSION_INTO_PLAY;
            assert _spacelineLocations.get(indexNumber).getMissions().size() == 1;
            missionCard.stackOn(_spacelineLocations.get(indexNumber).getMissions().iterator().next());
            _spacelineLocations.get(indexNumber).addMission(missionCard);
        } else {
            eventType = GameEvent.Type.PUT_CARD_INTO_PLAY;
            _spacelineLocations.add(indexNumber, new Location(missionCard));
        }
        refreshSpacelineIndices();
        addCardToZone(_game, missionCard, Zone.SPACELINE, true, eventType);
    }

    public void refreshSpacelineIndices() {
        for (int i = 0; i < _spacelineLocations.size(); i++) {
            for (PhysicalCard mission : _spacelineLocations.get(i).getMissions())
                mission.setLocationZoneIndex(i);
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
        for (Location location : _spacelineLocations) {
            if (location.getQuadrant() == quadrant) x++;
        }
        return x;
    }

    public Set<PhysicalCard> getQuadrantLocationCards(Quadrant quadrant) {
        Set<PhysicalCard> newCollection = new HashSet<>();
        for (Location location : _spacelineLocations)
            for (PhysicalCard mission : location.getMissions())
                if (mission.getQuadrant() == quadrant)
                    newCollection.add(mission);
        return newCollection;
    }

    @Override
    protected void sendStateToPlayer(String playerId, GameStateListener listener, GameStats gameStats) {
        if (_playerOrder != null) {
            listener.initializeBoard(_playerOrder.getAllPlayers(), _format.discardPileIsPublic());
            if (_currentPlayerId != null)
                listener.setCurrentPlayerId(_currentPlayerId);
            if (_currentPhase != null)
                listener.setCurrentPhase(getPhaseString());

            Set<PhysicalCard> cardsLeftToSend = new LinkedHashSet<>(_inPlay);
            Set<PhysicalCard> sentCardsFromPlay = new HashSet<>();

            // Send missions in order
            for (Location location : _spacelineLocations) {
                for (int i = 0; i < location.getMissions().size(); i++) {
                    GameEvent.Type eventType;
                    if (i == 0) {
                        eventType = GameEvent.Type.PUT_CARD_INTO_PLAY;
                    } else {
                        eventType = GameEvent.Type.PUT_SHARED_MISSION_INTO_PLAY;
                    }
                    listener.cardCreated(location.getMissions().get(i), eventType);
                    cardsLeftToSend.remove(location.getMissions().get(i));
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
                        listener.putCardIntoPlay(physicalCard);
                        sentCardsFromPlay.add(physicalCard);

                        cardIterator.remove();
                    }
                }
            } while (cardsToSendAtLoopStart != cardsLeftToSend.size() && cardsLeftToSend.size() > 0);

            List<PhysicalCard> hand = _hands.get(playerId);
            if (hand != null) hand.forEach(listener::putCardIntoPlay);

            List<PhysicalCard> missionPile = _missionPiles.get(playerId);
            if (missionPile != null) missionPile.forEach(listener::putCardIntoPlay);

            List<PhysicalCard> discard = _discards.get(playerId);
            if (discard != null) discard.forEach(listener::putCardIntoPlay);

            listener.sendGameStats(gameStats);
        }

        for (String lastMessage : _lastMessages)
            listener.sendMessage(lastMessage);

        final AwaitingDecision awaitingDecision = _playerDecisions.get(playerId);
        if (awaitingDecision != null)
            listener.decisionRequired(playerId, awaitingDecision);
    }

}