package com.gempukku.lotro.gamestate;

import com.gempukku.lotro.cards.CardBlueprintLibrary;
import com.gempukku.lotro.cards.CardDeck;
import com.gempukku.lotro.cards.CardNotFoundException;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.decisions.AwaitingDecision;
import com.gempukku.lotro.game.GameFormat;
import com.gempukku.lotro.game.ST1EGame;

import java.util.*;

public class ST1EGameState extends GameState {
    private Map<String, List<PhysicalCard>> _seedDecks;
    private Map<String, List<PhysicalCard>> _missionPiles;
    private Map<Quadrant, List<PhysicalCard>> _spacelines;
    private Map<String, List<PhysicalCard>> _tableCards;
    private ST1EGame _game;

    public ST1EGameState(Set<String> players, Map<String, CardDeck> decks, CardBlueprintLibrary library, GameFormat format, ST1EGame game) {
        super(players, decks, library, format);
        _format = format;
        _game = game;
        _seedDecks = new HashMap<>();
        _missionPiles = new HashMap<>();
        _spacelines = new HashMap<>();
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
                        subDeck.add(new PhysicalCard(_nextCardId, blueprintId, playerId, _library.getLotroCardBlueprint(blueprintId)));
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
    public PhysicalCard getTopOfMissionPile(String playerId) {
        return _missionPiles.get(playerId).get(0);
    }

    public List<PhysicalCard> getSpaceline(Quadrant quadrant) {
        return Collections.unmodifiableList(_spacelines.get(quadrant));
    }

    public void createNewSpaceline(Quadrant quadrant) {

        _spacelines.put(quadrant, new ArrayList<>());
        refreshSpacelineIndices();
    }

    public void addToSpaceline(PhysicalCard newMission, Quadrant quadrant, int indexNumber) {
        // TODO - Connect this to the server. See SWCCG code for GameState.addLocationToTable
        assignNewCardId(newMission);
        _spacelines.get(quadrant).add(indexNumber, newMission);
                // The function below is the meat of what happens here
        refreshSpacelineIndices(); // Check this against SW LocationsLayout.refreshLocationIndexes
        addCardToZone(_game, newMission, Zone.SPACELINE);
        for (GameStateListener listener : getAllGameStateListeners())
            listener.cardCreated(newMission);
        newMission.startAffectingGame(_game);
    }

    public void refreshSpacelineIndices() {
        int i = 0;
        if (spacelineExists(Quadrant.ALPHA)) {
            for (PhysicalCard card : _spacelines.get(Quadrant.ALPHA)) {
                card.setLocationZoneIndex(i);
                i++;
            }
        }
        if (spacelineExists(Quadrant.GAMMA)) {
            for (PhysicalCard card : _spacelines.get(Quadrant.GAMMA)) {
                card.setLocationZoneIndex(i);
                i++;
            }
        }
        if (spacelineExists(Quadrant.DELTA)) {
            for (PhysicalCard card : _spacelines.get(Quadrant.DELTA)) {
                card.setLocationZoneIndex(i);
                i++;
            }
        }
        if (spacelineExists(Quadrant.MIRROR)) {
            for (PhysicalCard card : _spacelines.get(Quadrant.MIRROR)) {
                card.setLocationZoneIndex(i);
                i++;
            }
        }
    }

    public boolean spacelineHasLocation(String location, Quadrant quadrant) {
        return _spacelines.get(quadrant).stream().anyMatch(card -> Objects.equals(card.getBlueprint().getLocation(), location));
    }

    public boolean spacelineHasRegion(Region region, Quadrant quadrant) {
        if (region == null)
            return false;
        else return _spacelines.get(quadrant).stream().anyMatch(
                card -> Objects.equals(card.getBlueprint().getRegion(), region)
        );
    }

    public boolean spacelineExists(Quadrant quadrant) { return _spacelines.containsKey(quadrant); }

    public int indexOfLocation(String location, Quadrant quadrant) {
        for (int i = 0; i < _spacelines.get(quadrant).size(); i++) {
            if (Objects.equals(_spacelines.get(quadrant).get(i).getBlueprint().getLocation(), location))
                return i;
        }
        return -1;
    }

    public PhysicalCard firstInRegion(Region region, Quadrant quadrant) {
        for (int i = 0; i < _spacelines.get(quadrant).size(); i++) {
            if (Objects.equals(_spacelines.get(quadrant).get(i).getBlueprint().getRegion(), region))
                return _spacelines.get(quadrant).get(i);
        }
        return null;
    }

    public PhysicalCard lastInRegion(Region region, Quadrant quadrant) {
        for (int i = _spacelines.get(quadrant).size() - 1; i >= 0; i--) {
            if (Objects.equals(_spacelines.get(quadrant).get(i).getBlueprint().getRegion(), region))
                return _spacelines.get(quadrant).get(i);
        }
        return null;
    }

    public int regionStartIndex(Region region, Quadrant quadrant) {
        for (int i = 0; i < _spacelines.get(quadrant).size(); i++) {
            if (Objects.equals(_spacelines.get(quadrant).get(i).getBlueprint().getRegion(), region))
                return i;
        }
        return -1;
    }

    public int regionEndIndex(Region region, Quadrant quadrant) {
        for (int i = _spacelines.get(quadrant).size() - 1; i >= 0; i--) {
            if (Objects.equals(_spacelines.get(quadrant).get(i).getBlueprint().getRegion(), region))
                return i;
        }
        return -1;
    }

    @Override
    public void playEffectReturningResult(PhysicalCard cardPlayed) { }

    @Override
    protected void sendStateToPlayer(String playerId, GameStateListener listener, GameStats gameStats) {
        if (_playerOrder != null) {
            listener.initializeBoard(_playerOrder.getAllPlayers(), _format.discardPileIsPublic());
            if (_currentPlayerId != null)
                listener.setCurrentPlayerId(_currentPlayerId);
            if (_currentPhase != null)
                listener.setCurrentPhase(getPhaseString());

            Set<PhysicalCard> cardsLeftToSent = new LinkedHashSet<>(_inPlay);
            Set<PhysicalCard> sentCardsFromPlay = new HashSet<>();

            int cardsToSendAtLoopStart;
            do {
                cardsToSendAtLoopStart = cardsLeftToSent.size();
                Iterator<PhysicalCard> cardIterator = cardsLeftToSent.iterator();
                while (cardIterator.hasNext()) {
                    PhysicalCard physicalCard = cardIterator.next();
                    PhysicalCard attachedTo = physicalCard.getAttachedTo();
                    if (attachedTo == null || sentCardsFromPlay.contains(attachedTo)) {
                        listener.cardCreated(physicalCard);
                        sentCardsFromPlay.add(physicalCard);

                        cardIterator.remove();
                    }
                }
            } while (cardsToSendAtLoopStart != cardsLeftToSent.size() && cardsLeftToSent.size() > 0);

            List<PhysicalCard> hand = _hands.get(playerId);
            if (hand != null) hand.forEach(listener::cardCreated);

            List<PhysicalCard> missionPile = _missionPiles.get(playerId);
            if (missionPile != null) missionPile.forEach(listener::cardCreated);

            List<PhysicalCard> discard = _discards.get(playerId);
            if (discard != null) discard.forEach(listener::cardCreated);

            listener.sendGameStats(gameStats);
        }

        for (String lastMessage : _lastMessages)
            listener.sendMessage(lastMessage);

        final AwaitingDecision awaitingDecision = _playerDecisions.get(playerId);
        if (awaitingDecision != null)
            listener.decisionRequired(playerId, awaitingDecision);
    }

}