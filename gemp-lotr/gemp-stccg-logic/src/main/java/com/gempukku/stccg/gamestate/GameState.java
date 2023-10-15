package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.PlayerOrder;
import com.gempukku.stccg.modifiers.ModifierFlag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public abstract class GameState {
    private static final Logger LOGGER = LogManager.getLogger(GameState.class);
    private static final int LAST_MESSAGE_STORED_COUNT = 15;
    protected PlayerOrder _playerOrder;
    protected GameFormat _format;
    protected final Map<String, Player> _players = new HashMap<>();
    protected final Map<String, List<PhysicalCard>> _drawDecks = new HashMap<>();
    protected final Map<String, List<PhysicalCard>> _hands = new HashMap<>();
    protected final Map<String, List<PhysicalCard>> _discards = new HashMap<>();
    protected final Map<String, List<PhysicalCard>> _stacked = new HashMap<>();

    private final Map<String, List<PhysicalCard>> _voids = new HashMap<>();
    private final Map<String, List<PhysicalCard>> _voidsFromHand = new HashMap<>();
    protected final Map<String, List<PhysicalCard>> _removed = new HashMap<>();

    protected final List<PhysicalCard> _inPlay = new LinkedList<>();

    private final Map<Integer, PhysicalCard> _allCards = new HashMap<>();

    protected String _currentPlayerId;
    protected Phase _currentPhase;
    private int _twilightPool;

    private boolean _consecutiveAction;

    protected final Map<String, AwaitingDecision> _playerDecisions = new HashMap<>();

    protected final Set<GameStateListener> _gameStateListeners = new HashSet<>();
    protected final LinkedList<String> _lastMessages = new LinkedList<>();
    protected final Map<String, CardDeck> _decks;
    protected final CardBlueprintLibrary _library;

    protected int _nextCardId = 0;

    private int nextCardId() {
        return _nextCardId++;
    }

    public GameState(Set<String> players, Map<String, CardDeck> decks, CardBlueprintLibrary library, GameFormat format) {
        _format = format;
        _decks = decks;
        _library = library;
        for (String playerId : players) {
            _players.put(playerId, new Player(playerId));
            _drawDecks.put(playerId, new LinkedList<>());
            _hands.put(playerId, new LinkedList<>());
            _voids.put(playerId, new LinkedList<>());
            _voidsFromHand.put(playerId, new LinkedList<>());
            _removed.put(playerId, new LinkedList<>());
            _discards.put(playerId, new LinkedList<>());
        }
    }

    public void init(PlayerOrder playerOrder, String firstPlayer) {
        _playerOrder = playerOrder;
        setCurrentPlayerId(firstPlayer);
        for (GameStateListener listener : getAllGameStateListeners()) {
            listener.initializeBoard(playerOrder.getAllPlayers(), _format.discardPileIsPublic());
        }
    }

    public void finish() {
        for (GameStateListener listener : getAllGameStateListeners()) {
            listener.endGame();
        }

        if(_playerOrder == null || _playerOrder.getAllPlayers() == null)
            return;

        for (String playerId : _playerOrder.getAllPlayers()) {
            for(var card : getDrawDeck(playerId)) {
                for (GameStateListener listener : getAllGameStateListeners()) {
                    listener.cardCreated(card, true);
                }
            }
        }
    }

    public abstract void createPhysicalCards();

    public PhysicalCard createPhysicalCard(String playerId, CardBlueprintLibrary library, String blueprintId)
            throws CardNotFoundException {
        CardBlueprint card = library.getCardBlueprint(blueprintId);

        int cardId = nextCardId();
        PhysicalCard result = new PhysicalCard(cardId, blueprintId, playerId, card);

        _allCards.put(cardId, result);

        return result;
    }

    public boolean isConsecutiveAction() {
        return _consecutiveAction;
    }

    public void setConsecutiveAction(boolean consecutiveAction) {
        _consecutiveAction = consecutiveAction;
    }

    public PlayerOrder getPlayerOrder() {
        return _playerOrder;
    }

    public void addGameStateListener(String playerId, GameStateListener gameStateListener, GameStats gameStats) {
        _gameStateListeners.add(gameStateListener);
        sendStateToPlayer(playerId, gameStateListener, gameStats);
    }

    public void removeGameStateListener(GameStateListener gameStateListener) {
        _gameStateListeners.remove(gameStateListener);
    }

    Collection<GameStateListener> getAllGameStateListeners() {
        return Collections.unmodifiableSet(_gameStateListeners);
    }

    protected String getPhaseString() {
        return _currentPhase.getHumanReadable();
    }

    protected void sendStateToPlayer(String playerId, GameStateListener listener, GameStats gameStats) {
        if (_playerOrder != null) {
            listener.initializeBoard(_playerOrder.getAllPlayers(), _format.discardPileIsPublic());
            if (_currentPlayerId != null) listener.setCurrentPlayerId(_currentPlayerId);
            if (_currentPhase != null) listener.setCurrentPhase(getPhaseString());
            listener.setTwilight(_twilightPool);

            Set<PhysicalCard> cardsLeftToSend = new LinkedHashSet<>(_inPlay);
            Set<PhysicalCard> sentCardsFromPlay = new HashSet<>();

            do {
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
            } while (cardsLeftToSend.size() > 0);

            List<PhysicalCard> cardsPutIntoPlay = new LinkedList<>();
            _stacked.values().forEach(cardsPutIntoPlay::addAll);
            cardsPutIntoPlay.addAll(_hands.get(playerId));
            cardsPutIntoPlay.addAll(_discards.get(playerId));
            cardsPutIntoPlay.forEach(listener::putCardIntoPlay);

            listener.sendGameStats(gameStats);
        }

        for (String lastMessage : _lastMessages)
            listener.sendMessage(lastMessage);

        final AwaitingDecision awaitingDecision = _playerDecisions.get(playerId);
        if (awaitingDecision != null)
            listener.decisionRequired(playerId, awaitingDecision);
    }

    public void sendMessage(String message) {
        _lastMessages.add(message);
        if (_lastMessages.size() > LAST_MESSAGE_STORED_COUNT)
            _lastMessages.removeFirst();
        for (GameStateListener listener : getAllGameStateListeners())
            listener.sendMessage(message);
    }

    public void playerDecisionStarted(String playerId, AwaitingDecision awaitingDecision) {
        _playerDecisions.put(playerId, awaitingDecision);
        for (GameStateListener listener : getAllGameStateListeners())
            listener.decisionRequired(playerId, awaitingDecision);
    }

    public void playerDecisionFinished(String playerId) {
        _playerDecisions.remove(playerId);
    }

    public void transferCard(PhysicalCard card, PhysicalCard transferTo) {
        if (card.getZone() != Zone.ATTACHED)
            card.setZone(Zone.ATTACHED);

        card.attachTo(transferTo);
        for (GameStateListener listener : getAllGameStateListeners())
            listener.cardMoved(card);
    }

    public void attachCard(DefaultGame game, PhysicalCard card, PhysicalCard attachTo) throws InvalidParameterException {
        if(card == attachTo)
            throw new InvalidParameterException("Cannot attach card to itself!");

        card.attachTo(attachTo);
        addCardToZone(game, card, Zone.ATTACHED);
    }

    public void stackCard(DefaultGame game, PhysicalCard card, PhysicalCard stackOn) throws InvalidParameterException {
        if(card == stackOn)
            throw new InvalidParameterException("Cannot stack card on itself!");

        card.stackOn(stackOn);
        addCardToZone(game, card, Zone.STACKED);
    }

    public void cardAffectsCard(String playerPerforming, PhysicalCard card, Collection<PhysicalCard> affectedCards) {
        for (GameStateListener listener : getAllGameStateListeners())
            listener.cardAffectedByCard(playerPerforming, card, affectedCards);
    }

    public void eventPlayed(PhysicalCard card) {
        for (GameStateListener listener : getAllGameStateListeners())
            listener.eventPlayed(card);
    }

    public void activatedCard(String playerPerforming, PhysicalCard card) {
        for (GameStateListener listener : getAllGameStateListeners())
            listener.cardActivated(playerPerforming, card);
    }

    public List<PhysicalCard> getZoneCards(String playerId, Zone zone) {
        if (zone == Zone.DRAW_DECK)
            return _drawDecks.get(playerId);
        else if (zone == Zone.DISCARD)
            return _discards.get(playerId);
        else if (zone == Zone.HAND)
            return _hands.get(playerId);
        else if (zone == Zone.VOID)
            return _voids.get(playerId);
        else if (zone == Zone.VOID_FROM_HAND)
            return _voidsFromHand.get(playerId);
        else if (zone == Zone.REMOVED)
            return _removed.get(playerId);
        else if (zone == Zone.STACKED)
            return _stacked.get(playerId);
        else
            return _inPlay;
    }

    public void removeCardFromZone(PhysicalCard card) {
        removeCardsFromZone(card.getOwner(), Collections.singleton(card));
    }

    public void removeCardsFromZone(String playerPerforming, Collection<PhysicalCard> cards) {
        for (PhysicalCard card : cards) {
            List<PhysicalCard> zoneCards = getZoneCards(card.getOwner(), card.getZone());
            if (!zoneCards.contains(card))
                LOGGER.error("Card was not found in the expected zone");
        }

        for (PhysicalCard card : cards) {
            Zone zone = card.getZone();

            if (zone.isInPlay()) card.stopAffectingGame();
            if (zone == Zone.STACKED || zone == Zone.DISCARD)
                if (card.isAffectingGame(this)) card.stopAffectingGameInZone(zone);

            getZoneCards(card.getOwner(), zone).remove(card);

            if (zone.isInPlay())
                _inPlay.remove(card);
            if (zone == Zone.ATTACHED)
                card.attachTo(null);

            if (zone == Zone.STACKED)
                card.stackOn(null);

            //If this is reset, then there is no way for self-discounting effects (which are evaluated while in the void)
            // to have any sort of permanent effect once the card is in play.
            if(zone != Zone.VOID_FROM_HAND && zone != Zone.VOID)
                card.setWhileInZoneData(null);
        }

        for (GameStateListener listener : getAllGameStateListeners())
            listener.cardsRemoved(playerPerforming, cards);

        for (PhysicalCard card : cards) {
            card.setZone(null);
        }
    }

    public void addCardToZone(DefaultGame game, PhysicalCard card, Zone zone) {
        addCardToZone(game, card, zone, true);
    }
    public void addCardToZone(DefaultGame game, PhysicalCard card, Zone zone, EndOfPile endOfPile) {
        addCardToZone(game, card, zone, endOfPile != EndOfPile.TOP);
    }

    public void addCardToZone(DefaultGame game, PhysicalCard card, Zone zone, boolean end) {
        addCardToZone(game, card, zone, end, GameEvent.Type.PUT_CARD_INTO_PLAY);
    }

    public void addCardToZone(DefaultGame game, PhysicalCard card, Zone zone, boolean end, GameEvent.Type eventType) {
        if (zone == Zone.DISCARD && game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.REMOVE_CARDS_GOING_TO_DISCARD))
            zone = Zone.REMOVED;

        if (zone.isInPlay()) {
            assignNewCardId(card);
            _inPlay.add(card);
        }

        List<PhysicalCard> zoneCards = getZoneCards(card.getOwner(), zone);
        if (end)
            zoneCards.add(card);
        else
            zoneCards.add(0, card);

        if (card.getZone() != null)
            LOGGER.error("Card was in " + card.getZone() + " when tried to add to zone: " + zone);

        card.setZone(zone);

        for (GameStateListener listener : getAllGameStateListeners())
            listener.cardCreated(card, eventType);

//        if (_currentPhase.isCardsAffectGame()) {
        if (zone.isInPlay())
            card.startAffectingGame(game);
        if ((zone == Zone.STACKED || zone == Zone.DISCARD) && card.isAffectingGame(this))
            card.startAffectingGameInZone(game, zone);
    }

    void assignNewCardId(PhysicalCard card) {
        _allCards.remove(card.getCardId());
        int newCardId = nextCardId();
        card.setCardId(newCardId);
        _allCards.put(newCardId, card);
    }

    public void shuffleCardsIntoDeck(Collection<? extends PhysicalCard> cards, String playerId) {
        List<PhysicalCard> zoneCards = _drawDecks.get(playerId);

        for (PhysicalCard card : cards) {
            zoneCards.add(card);

            card.setZone(Zone.DRAW_DECK);
        }

        shuffleDeck(playerId);
    }

    public void putCardOnBottomOfDeck(PhysicalCard card) {
        addCardToZone(null, card, Zone.DRAW_DECK, true);
    }

    public void putCardOnTopOfDeck(PhysicalCard card) {
        addCardToZone(null, card, Zone.DRAW_DECK, false);
    }

    public void iterateActiveCards(PhysicalCardVisitor physicalCardVisitor) {
        for (PhysicalCard physicalCard : _inPlay) {
            if (isCardInPlayActive(physicalCard))
                if (physicalCardVisitor.visitPhysicalCard(physicalCard))
                    return;
        }

    }

    public PhysicalCard findCardById(int cardId) {
        return _allCards.get(cardId);
    }

    public Iterable<PhysicalCard> getAllCardsInGame() { return Collections.unmodifiableCollection(_allCards.values()); }
    public List<PhysicalCard> getAllCardsInPlay() {
        return Collections.unmodifiableList(_inPlay);
    }
    public List<PhysicalCard> getHand(String playerId) { return Collections.unmodifiableList(_hands.get(playerId)); }
    public List<PhysicalCard> getVoidFromHand(String playerId) { return Collections.unmodifiableList(_voidsFromHand.get(playerId)); }
    public List<PhysicalCard> getRemoved(String playerId) { return Collections.unmodifiableList(_removed.get(playerId)); }
    public List<PhysicalCard> getDrawDeck(String playerId) { return Collections.unmodifiableList(_drawDecks.get(playerId)); }
    public List<PhysicalCard> getDiscard(String playerId) { return Collections.unmodifiableList(_discards.get(playerId)); }
    public List<PhysicalCard> getStacked(String playerId) { return Collections.unmodifiableList(_stacked.get(playerId)); }

    public String getCurrentPlayerId() {
        return _playerOrder.getCurrentPlayer();
    }

    public void setCurrentPlayerId(String playerId) {
        _playerOrder.setCurrentPlayer(playerId);
    }

    public List<PhysicalCard> getAttachedCards(PhysicalCard card) {
        List<PhysicalCard> result = new LinkedList<>();
        for (PhysicalCard physicalCard : _inPlay) {
            if (physicalCard.getAttachedTo() != null && physicalCard.getAttachedTo() == card)
                result.add(physicalCard);
        }
        return result;
    }

    public List<PhysicalCard> getStackedCards(PhysicalCard card) {
        List<PhysicalCard> result = new LinkedList<>();
        for (List<PhysicalCard> physicalCardList : _stacked.values()) {
            for (PhysicalCard physicalCard : physicalCardList) {
                if (physicalCard.getStackedOn() == card)
                    result.add(physicalCard);
            }
        }
        return result;
    }

    public void setTwilight(int twilight) {
        _twilightPool = twilight;
        getAllGameStateListeners().forEach(listener -> listener.setTwilight(_twilightPool));
    }

    public int getTwilightPool() {
        return _twilightPool;
    }

    public void startPlayerTurn(String playerId) {
        _playerOrder.setCurrentPlayer(playerId);
        getAllGameStateListeners().forEach(listener -> listener.setCurrentPlayerId(playerId));
    }

    public boolean isCardInPlayActive(PhysicalCard card) {
        if (card.getAttachedTo() != null) return isCardInPlayActive(card.getAttachedTo());
        else return true;
    }

    public void startAffectingCardsForCurrentPlayer(DefaultGame game) {
        // Active non-sites are affecting
        for (PhysicalCard physicalCard : _inPlay)
            if (isCardInPlayActive(physicalCard)) physicalCard.startAffectingGame(game);

        // Stacked cards on active cards are stack-affecting
        for (List<PhysicalCard> stackedCards : _stacked.values())
            for (PhysicalCard stackedCard : stackedCards)
                if ((isCardInPlayActive(stackedCard.getStackedOn()) && stackedCard.isAffectingGame(this)))
                    stackedCard.startAffectingGameInZone(game, Zone.STACKED);

        for (List<PhysicalCard> discardedCards : _discards.values())
            for (PhysicalCard discardedCard : discardedCards)
                if (discardedCard.isAffectingGame(this))
                    discardedCard.startAffectingGameInZone(game, Zone.DISCARD);
    }

    public void stopAffectingCardsForCurrentPlayer() {
        for (PhysicalCard physicalCard : _inPlay)
            physicalCard.stopAffectingGame();

        for (List<PhysicalCard> stackedCards : _stacked.values())
            for (PhysicalCard stackedCard : stackedCards)
                if (isCardInPlayActive(stackedCard.getStackedOn()))
                    if (stackedCard.isAffectingGame(this)) stackedCard.stopAffectingGameInZone(Zone.STACKED);

        for (List<PhysicalCard> discardedCards : _discards.values())
            for (PhysicalCard discardedCard : discardedCards)
                if (discardedCard.isAffectingGame(this)) discardedCard.stopAffectingGameInZone(Zone.DISCARD);
    }

    public void setCurrentPhase(Phase phase) {
        _currentPhase = phase;
        for (GameStateListener listener : getAllGameStateListeners())
            listener.setCurrentPhase(getPhaseString());
    }

    public Phase getCurrentPhase() {
        return _currentPhase;
    }

    public void addTwilight(int twilight) {
        setTwilight(_twilightPool + Math.max(0, twilight));
    }

    public void removeTwilight(int twilight) {
        setTwilight(_twilightPool - Math.min(Math.max(0, twilight), _twilightPool));
    }

    public PhysicalCard removeCardFromEndOfPile(String player, Zone zone, EndOfPile endOfPile) {
        List<PhysicalCard> deck = getZoneCards(player, zone);
        int pileIndex = endOfPile == EndOfPile.BOTTOM ? deck.size() - 1 : 0;
        if (deck.size() > 0) {
            final PhysicalCard removedCard = deck.get(pileIndex);
            removeCardsFromZone(null, Collections.singleton(removedCard));
            return removedCard;
        } else {
            return null;
        }
    }

    public void playerDrawsCard(String player) {
        List<PhysicalCard> deck = _drawDecks.get(player);
        if (deck.size() > 0) {
            PhysicalCard card = deck.get(0);
            removeCardsFromZone(null, Collections.singleton(card));
            addCardToZone(null, card, Zone.HAND);
        }
    }

    public void shuffleDeck(String player) {
        List<PhysicalCard> deck = _drawDecks.get(player);
        Collections.shuffle(deck, ThreadLocalRandom.current());
    }

    public void sendGameStats(GameStats gameStats) {
        for (GameStateListener listener : getAllGameStateListeners())
            listener.sendGameStats(gameStats);
    }

    public void sendWarning(String player, String warning) {
        for (GameStateListener listener : getAllGameStateListeners())
            listener.sendWarning(player, warning);
    }

    public void addToPlayerScore(String player, int points) {
        _players.get(player).scorePoints(points);
        for (GameStateListener listener : getAllGameStateListeners())
            listener.setPlayerScore(player, getPlayerScore(player));
    }

    public int getPlayerScore(String playerId) {
        return _players.get(playerId).getScore();
    }

    public void discardHand(DefaultGame game, String playerId) {
        List<PhysicalCard> hand = new LinkedList<>(getHand(playerId));
        removeCardsFromZone(playerId, hand);
        for (PhysicalCard card : hand) {
            addCardToZone(game, card, Zone.DISCARD);
        }
    }
}