package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalReportableCard1E;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.common.AwaitingDecision;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST2EGame;

import java.util.*;

public class ST2EGameState extends GameState {
    private final Map<String, List<PhysicalCard>> _seedDecks;
    private final Map<String, List<PhysicalCard>> _missionPiles;
    private final List<ST1ELocation> _spacelineLocations = new ArrayList<>();
    private final Map<String, List<PhysicalCard>> _tableCards;
    private final ST2EGame _game;

    public ST2EGameState(Set<String> players, Map<String, CardDeck> decks, CardBlueprintLibrary library, GameFormat format, ST2EGame game) {
        super(players, decks, library, format, game);
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
    public ST2EGame getGame() { return _game; }

    @Override
    public List<PhysicalCard> getZoneCards(String playerId, Zone zone) {
        if (zone == Zone.DRAW_DECK || zone == Zone.HAND || zone == Zone.DISCARD || zone == Zone.REMOVED)
            return _cardGroups.get(zone).get(playerId);
        else if (zone == Zone.STACKED)
            return _stacked.get(playerId);
        else if (zone == Zone.TABLE)
            return _tableCards.get(playerId);
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
                        subDeck.add(_library.getCardBlueprint(blueprintId).createPhysicalCard(getGame(),
                                _nextCardId, player));
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
                    sendCreatedCardToListener(location.getMissions().get(i), false, listener, true);
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
                        sendCreatedCardToListener(physicalCard, false, listener, !restoreSnapshot);
                        sentCardsFromPlay.add(physicalCard);

                        cardIterator.remove();
                    }
                }
            } while (cardsToSendAtLoopStart != cardsLeftToSend.size() && !cardsLeftToSend.isEmpty());

            for (PhysicalCard physicalCard : _cardGroups.get(Zone.HAND).get(playerId))
                sendCreatedCardToListener(physicalCard, false, listener, !restoreSnapshot);

            List<PhysicalCard> missionPile = _missionPiles.get(playerId);
            if (missionPile != null) {
                for (PhysicalCard physicalCard : missionPile) {
                    sendCreatedCardToListener(physicalCard, false, listener, !restoreSnapshot);
                }
            }

            for (PhysicalCard physicalCard : _cardGroups.get(Zone.DISCARD).get(playerId)) {
                sendCreatedCardToListener(physicalCard, false, listener, !restoreSnapshot);
            }

            listener.sendEvent(new GameEvent(GameEvent.Type.GAME_STATS, getGame().getTurnProcedure().getGameStats()));
        }

        for (String lastMessage : _lastMessages)
            listener.sendMessage(lastMessage);

        final AwaitingDecision awaitingDecision = _playerDecisions.get(playerId);
        if (awaitingDecision != null)
            listener.decisionRequired(playerId, awaitingDecision);
    }

}