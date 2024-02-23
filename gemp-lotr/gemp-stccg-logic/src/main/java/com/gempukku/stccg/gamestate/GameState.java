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
    protected Map<Zone, Map<String, List<PhysicalCard>>> _cardGroups = new HashMap<>();

    protected final Map<String, Player> _players = new HashMap<>();
    protected final Map<String, List<PhysicalCard>> _stacked = new HashMap<>();

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

    public GameState(Set<String> players, Map<String, CardDeck> decks, CardBlueprintLibrary library,
                     GameFormat format, DefaultGame game) {
        _format = format;
        _decks = decks;
        _library = library;
        Collection<Zone> cardGroupList = new LinkedList<>();
        cardGroupList.add(Zone.DRAW_DECK);
        cardGroupList.add(Zone.HAND);
        cardGroupList.add(Zone.VOID);
        cardGroupList.add(Zone.VOID_FROM_HAND);
        cardGroupList.add(Zone.DISCARD);
        cardGroupList.add(Zone.REMOVED);

        cardGroupList.forEach(cardGroup -> _cardGroups.put(cardGroup, new HashMap<>()));
        for (String playerId : players) {
            cardGroupList.forEach(cardGroup -> _cardGroups.get(cardGroup).put(playerId, new LinkedList<>()));
            _players.put(playerId, new Player(game, playerId));
        }
    }

    public abstract DefaultGame getGame();

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

        int cardId = _nextCardId++;
        PhysicalCard result = new PhysicalCardGeneric(getGame(), cardId, playerId, card);

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
        sendStateToPlayer(playerId, gameStateListener);
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

    protected void sendStateToPlayer(String playerId, GameStateListener listener) {
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
            } while (!cardsLeftToSend.isEmpty());

            List<PhysicalCard> cardsPutIntoPlay = new LinkedList<>();
            _stacked.values().forEach(cardsPutIntoPlay::addAll);
            cardsPutIntoPlay.addAll(_cardGroups.get(Zone.HAND).get(playerId));
            cardsPutIntoPlay.addAll(_cardGroups.get(Zone.DISCARD).get(playerId));
            cardsPutIntoPlay.forEach(listener::putCardIntoPlay);

            listener.sendGameStats(getGame().getTurnProcedure().getGameStats());
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

    public void detachCard(PhysicalCard attachedCard, Zone newZone) {
        attachedCard.setZone(newZone);
        attachedCard.detach();
        for (GameStateListener listener : getAllGameStateListeners())
            listener.cardMoved(attachedCard);
    }

    public void attachCard(PhysicalCard card, PhysicalCard attachTo) throws InvalidParameterException {
        if(card == attachTo)
            throw new InvalidParameterException("Cannot attach card to itself!");

        card.attachTo(attachTo);
        addCardToZone(card, Zone.ATTACHED);
    }

    public void stackCard(PhysicalCard card, PhysicalCard stackOn) throws InvalidParameterException {
        if(card == stackOn)
            throw new InvalidParameterException("Cannot stack card on itself!");

        card.stackOn(stackOn);
        addCardToZone(card, Zone.STACKED);
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
        List<PhysicalCard> zoneCards = _cardGroups.get(zone).get(playerId);
        if (zoneCards != null)
            return zoneCards;
        else if (zone == Zone.STACKED)
            return _stacked.get(playerId);
        else
            return _inPlay;
    }

    public void removeCardFromZone(PhysicalCard card) {
        removeCardsFromZone(card.getOwnerName(), Collections.singleton(card));
    }

    public void removeCardsFromZone(String playerPerforming, Collection<PhysicalCard> cards) {
        for (PhysicalCard card : cards) {
            List<PhysicalCard> zoneCards = getZoneCards(card.getOwnerName(), card.getZone());
            if (!zoneCards.contains(card))
                LOGGER.error("Card was not found in the expected zone");
        }

        for (PhysicalCard card : cards) {
            Zone zone = card.getZone();

            if (zone.isInPlay()) card.stopAffectingGame();
            if (zone == Zone.STACKED || zone == Zone.DISCARD)
                if (card.isAffectingGame()) card.stopAffectingGameInZone(zone);

            getZoneCards(card.getOwnerName(), zone).remove(card);

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

    public void addCardToZone(PhysicalCard card, Zone zone) {
        addCardToZone(card, zone, true);
    }
    public void addCardToZone(PhysicalCard card, Zone zone, EndOfPile endOfPile) {
        addCardToZone(card, zone, endOfPile != EndOfPile.TOP);
    }

    public void addCardToZone(PhysicalCard card, Zone zone, boolean end) {
        addCardToZone(card, zone, end, GameEvent.Type.PUT_CARD_INTO_PLAY);
    }

    public void addCardToZone(PhysicalCard card, Zone zone, boolean end, GameEvent.Type eventType) {
        if (zone == Zone.DISCARD &&
                getGame().getModifiersQuerying().hasFlagActive(getGame(), ModifierFlag.REMOVE_CARDS_GOING_TO_DISCARD))
            zone = Zone.REMOVED;

        if (zone.isInPlay()) {
            assignNewCardId(card);
            _inPlay.add(card);
        }

        if (zone.hasList()) {
            List<PhysicalCard> zoneCardList = getZoneCards(card.getOwnerName(), zone);
            if (end)
                zoneCardList.add(card);
            else
                zoneCardList.add(0, card);
        }

        if (card.getZone() != null)
            LOGGER.error("Card was in " + card.getZone() + " when tried to add to zone: " + zone);

        card.setZone(zone);

        for (GameStateListener listener : getAllGameStateListeners())
            listener.cardCreated(card, eventType);

//        if (_currentPhase.isCardsAffectGame()) {
        if (zone.isInPlay())
            card.startAffectingGame();
        if ((zone == Zone.STACKED || zone == Zone.DISCARD) && card.isAffectingGame())
            card.startAffectingGameInZone(zone);
    }

    void assignNewCardId(PhysicalCard card) {
        _allCards.remove(card.getCardId());
        int newCardId = _nextCardId++;
        card.setCardId(newCardId);
        _allCards.put(newCardId, card);
    }

    public void shuffleCardsIntoDeck(Collection<? extends PhysicalCard> cards, String playerId) {

        for (PhysicalCard card : cards) {
            _cardGroups.get(Zone.DRAW_DECK).get(playerId).add(card);
            card.setZone(Zone.DRAW_DECK);
        }

        shuffleDeck(playerId);
    }

    public void putCardOnBottomOfDeck(PhysicalCard card) {
        addCardToZone(card, Zone.DRAW_DECK, true);
    }

    public void putCardOnTopOfDeck(PhysicalCard card) {
        addCardToZone(card, Zone.DRAW_DECK, false);
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
    public List<PhysicalCard> getHand(String playerId) { return getCardGroup(Zone.HAND, playerId); }
    public List<PhysicalCard> getVoidFromHand(String playerId) { return getCardGroup(Zone.VOID_FROM_HAND, playerId); }
    public List<PhysicalCard> getRemoved(String playerId) { return getCardGroup(Zone.REMOVED, playerId); }
    public List<PhysicalCard> getDrawDeck(String playerId) { return getCardGroup(Zone.DRAW_DECK, playerId); }

    private List<PhysicalCard> getCardGroup(Zone zone, String playerId) {
        return Collections.unmodifiableList(_cardGroups.get(zone).get(playerId));
    }
    public List<PhysicalCard> getDiscard(String playerId) { return getCardGroup(Zone.DISCARD, playerId); }

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

    public Map<String, List<PhysicalCard>> getStackedCards() {
        return _stacked;
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

    public void startAffectingCardsForCurrentPlayer() {
        // Active non-sites are affecting
        for (PhysicalCard physicalCard : _inPlay)
            if (isCardInPlayActive(physicalCard)) physicalCard.startAffectingGame();

        // Stacked cards on active cards are stack-affecting
        for (List<PhysicalCard> stackedCards : _stacked.values())
            for (PhysicalCard stackedCard : stackedCards)
                if ((isCardInPlayActive(stackedCard.getStackedOn()) && stackedCard.isAffectingGame()))
                    stackedCard.startAffectingGameInZone(Zone.STACKED);

        for (List<PhysicalCard> discardedCards : _cardGroups.get(Zone.DISCARD).values())
            for (PhysicalCard discardedCard : discardedCards)
                if (discardedCard.isAffectingGame())
                    discardedCard.startAffectingGameInZone(Zone.DISCARD);
    }

    public void stopAffectingCardsForCurrentPlayer() {
        for (PhysicalCard physicalCard : _inPlay)
            physicalCard.stopAffectingGame();

        for (List<PhysicalCard> stackedCards : _stacked.values())
            for (PhysicalCard stackedCard : stackedCards)
                if (isCardInPlayActive(stackedCard.getStackedOn()))
                    if (stackedCard.isAffectingGame()) stackedCard.stopAffectingGameInZone(Zone.STACKED);

        for (List<PhysicalCard> discardedCards : _cardGroups.get(Zone.DISCARD).values())
            for (PhysicalCard discardedCard : discardedCards)
                if (discardedCard.isAffectingGame()) discardedCard.stopAffectingGameInZone(Zone.DISCARD);
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
        if (!deck.isEmpty()) {
            final PhysicalCard removedCard = deck.get(pileIndex);
            removeCardsFromZone(null, Collections.singleton(removedCard));
            return removedCard;
        } else {
            return null;
        }
    }

    public void playerDrawsCard(String player) {
        playerDrawsCard(null, player);
    }

    public void playerDrawsCard(DefaultGame game, String playerId) {
        List<PhysicalCard> deck = _cardGroups.get(Zone.DRAW_DECK).get(playerId);
        if (!deck.isEmpty()) {
            PhysicalCard card = deck.get(0);
            removeCardsFromZone(playerId, Collections.singleton(card));
            addCardToZone(card, Zone.HAND);
        }
    }

    public void shuffleDeck(String playerId) {
        Collections.shuffle(_cardGroups.get(Zone.DRAW_DECK).get(playerId), ThreadLocalRandom.current());
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

    public Player getPlayer(String playerId) { return _players.get(playerId); }
    public Collection<Player> getPlayers() { return _players.values(); }

    public void discardHand(DefaultGame game, String playerId) {
        List<PhysicalCard> hand = new LinkedList<>(getHand(playerId));
        removeCardsFromZone(playerId, hand);
        for (PhysicalCard card : hand) {
            addCardToZone(card, Zone.DISCARD);
        }
    }
}