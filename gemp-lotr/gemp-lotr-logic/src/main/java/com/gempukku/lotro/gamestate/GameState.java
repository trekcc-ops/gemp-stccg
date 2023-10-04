package com.gempukku.lotro.gamestate;

import com.gempukku.lotro.cards.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.decisions.AwaitingDecision;
import com.gempukku.lotro.game.*;
import com.gempukku.lotro.modifiers.ModifierFlag;
import org.apache.log4j.Logger;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public abstract class GameState {
    private static final Logger _log = Logger.getLogger(GameState.class);
    private static final int LAST_MESSAGE_STORED_COUNT = 15;
    protected PlayerOrder _playerOrder;
    protected GameFormat _format;
    protected final Map<String, Player> _players = new HashMap<>();
    protected final Map<String, List<PhysicalCard>> _drawDecks = new HashMap<>();
    protected final Map<String, List<PhysicalCard>> _hands = new HashMap<>();
    protected final Map<String, List<PhysicalCard>> _discards = new HashMap<>();
    private final Map<String, List<PhysicalCard>> _deadPiles = new HashMap<>();
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

    private final Map<String, Integer> _playerPosition = new HashMap<>();
    private final Map<String, Integer> _playerThreats = new HashMap<>();

    private final Map<PhysicalCard, Map<Token, Integer>> _cardTokens = new HashMap<>();

    protected final Map<String, AwaitingDecision> _playerDecisions = new HashMap<>();

    protected final Set<GameStateListener> _gameStateListeners = new HashSet<>();
    protected final LinkedList<String> _lastMessages = new LinkedList<>();
    protected Map<String, CardDeck> _decks;
    protected CardBlueprintLibrary _library;

    private int _nextCardId = 0;

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
        LotroCardBlueprint card = library.getLotroCardBlueprint(blueprintId);

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
            if (_currentPlayerId != null)
                listener.setCurrentPlayerId(_currentPlayerId);
            if (_currentPhase != null)
                listener.setCurrentPhase(getPhaseString());
            listener.setTwilight(_twilightPool);
            for (Map.Entry<String, Integer> stringIntegerEntry : _playerPosition.entrySet())
                listener.setPlayerPosition(stringIntegerEntry.getKey(), stringIntegerEntry.getValue());

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

            // Finally the stacked ones
            for (List<PhysicalCard> physicalCards : _stacked.values())
                for (PhysicalCard physicalCard : physicalCards)
                    listener.cardCreated(physicalCard);

            List<PhysicalCard> hand = _hands.get(playerId);
            if (hand != null) {
                for (PhysicalCard physicalCard : hand)
                    listener.cardCreated(physicalCard);
            }

            _deadPiles.values().stream().flatMap(Collection::stream).forEach(listener::cardCreated);

            List<PhysicalCard> discard = _discards.get(playerId);
            if (discard != null) {
                discard.forEach(listener::cardCreated);
            }

            for (Map.Entry<PhysicalCard, Map<Token, Integer>> physicalCardMapEntry : _cardTokens.entrySet()) {
                PhysicalCard card = physicalCardMapEntry.getKey();
                for (Map.Entry<Token, Integer> tokenIntegerEntry : physicalCardMapEntry.getValue().entrySet()) {
                    Integer count = tokenIntegerEntry.getValue();
                    if (count != null && count > 0)
                        listener.addTokens(card, tokenIntegerEntry.getKey(), count);
                }
            }

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

    public void takeControlOfCard(String playerId, DefaultGame game, PhysicalCard card, Zone zone) {
        card.setCardController(playerId);
        card.setZone(zone);
        if (card.getBlueprint().getCardType() == CardType.SITE)
            card.startAffectingGameControlledSite(game);
        for (GameStateListener listener : getAllGameStateListeners())
            listener.cardMoved(card);
    }

    public void loseControlOfCard(PhysicalCard card, Zone zone) {
        card.setCardController(null);
        card.setZone(zone);
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

    public PhysicalCard getRingBearer(String playerId) {
        return null;
    }

    public List<PhysicalCard> getZoneCards(String playerId, Zone zone) {
        if (zone == Zone.DRAW_DECK)
            return _drawDecks.get(playerId);
        else if (zone == Zone.DISCARD)
            return _discards.get(playerId);
        else if (zone == Zone.DEAD)
            return _deadPiles.get(playerId);
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

    public void removeCardsFromZone(String playerPerforming, Collection<PhysicalCard> cards) {
        for (PhysicalCard card : cards) {
            List<PhysicalCard> zoneCards = getZoneCards(card.getOwner(), card.getZone());
            if (!zoneCards.contains(card))
                _log.error("Card was not found in the expected zone");
        }

        for (PhysicalCard card : cards) {
            Zone zone = card.getZone();

            if (zone.isInPlay())
                if (card.getBlueprint().getCardType() != CardType.SITE ||
                        (getCurrentPhase() != Phase.PLAY_STARTING_FELLOWSHIP && getCurrentSite() == card))
                    card.stopAffectingGame();

            if (zone == Zone.STACKED)
                stopAffectingStacked(card);
            else if (zone == Zone.DISCARD)
                stopAffectingInDiscard(card);

            List<PhysicalCard> zoneCards = getZoneCards(card.getOwner(), zone);
            zoneCards.remove(card);

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
            _log.error("Card was in " + card.getZone() + " when tried to add to zone: " + zone);

        card.setZone(zone);

        if (zone == Zone.ADVENTURE_PATH) {
            for (GameStateListener listener : getAllGameStateListeners())
                listener.setSite(card);
        } else {
            for (GameStateListener listener : getAllGameStateListeners())
                listener.cardCreated(card);
        }

//        if (_currentPhase.isCardsAffectGame()) {
        if (zone.isInPlay())
            card.startAffectingGame(game);

        if (zone == Zone.STACKED)
            startAffectingStacked(game, card);
        else if (zone == Zone.DISCARD)
            startAffectingInDiscard(game, card);
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

    public Iterable<? extends PhysicalCard> getAllCards() {
        return Collections.unmodifiableCollection(_allCards.values());
    }

    public List<? extends PhysicalCard> getHand(String playerId) {
        return Collections.unmodifiableList(_hands.get(playerId));
    }

    public List<? extends PhysicalCard> getVoidFromHand(String playerId) {
        return Collections.unmodifiableList(_voidsFromHand.get(playerId));
    }

    public List<? extends PhysicalCard> getRemoved(String playerId) {
        return Collections.unmodifiableList(_removed.get(playerId));
    }

    public List<? extends PhysicalCard> getDrawDeck(String playerId) {
        return Collections.unmodifiableList(_drawDecks.get(playerId));
    }

    public List<PhysicalCard> getDiscard(String playerId) {
        return Collections.unmodifiableList(_discards.get(playerId));
    }

    public List<? extends PhysicalCard> getDeadPile(String playerId) {
        return Collections.unmodifiableList(_deadPiles.get(playerId));
    }

    public List<? extends PhysicalCard> getInPlay() {
        return Collections.unmodifiableList(_inPlay);
    }

    public List<? extends PhysicalCard> getStacked(String playerId) {
        return Collections.unmodifiableList(_stacked.get(playerId));
    }

    public String getCurrentPlayerId() {
        return _playerOrder.getCurrentPlayer();
    }

    public void setCurrentPlayerId(String playerId) {
        _playerOrder.setCurrentPlayer(playerId);
    }

    public int getCurrentSiteNumber() {
        return _playerPosition.getOrDefault(_currentPlayerId, 0);
    }

    public void removeThreats(String playerId, int count) {
        final int oldThreats = _playerThreats.get(playerId);
        count = Math.min(count, oldThreats);
        _playerThreats.put(playerId, oldThreats - count);
    }

    public int getPlayerPosition(String playerId) {
        return _playerPosition.getOrDefault(playerId, 0);
    }

    public Map<Token, Integer> getTokens(PhysicalCard card) {
        Map<Token, Integer> map = _cardTokens.get(card);
        if (map == null)
            return Collections.emptyMap();
        return Collections.unmodifiableMap(map);
    }

    public int getTokenCount(PhysicalCard physicalCard, Token token) {
        Map<Token, Integer> tokens = _cardTokens.get(physicalCard);
        if (tokens == null)
            return 0;
        Integer count = tokens.get(token);
        if (count == null)
            return 0;
        return count;
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

    public int getWounds(PhysicalCard physicalCard) {
        return getTokenCount(physicalCard, Token.WOUND);
    }

    public int getBurdens() {
        return 0;
    }

    public int getThreats() {
        return _playerThreats.get(getCurrentPlayerId());
    }

    public void addWound(PhysicalCard card) {
        addTokens(card, Token.WOUND, 1);
    }

    public void removeWound(PhysicalCard card) {
        removeTokens(card, Token.WOUND, 1);
    }

    public void addTokens(PhysicalCard card, Token token, int count) {
        Map<Token, Integer> tokens = _cardTokens.computeIfAbsent(card, k -> new HashMap<>());
        tokens.merge(token, count, Integer::sum);

        for (GameStateListener listener : getAllGameStateListeners())
            listener.addTokens(card, token, count);
    }

    public void removeTokens(PhysicalCard card, Token token, int count) {
        Map<Token, Integer> tokens = _cardTokens.computeIfAbsent(card, k -> new HashMap<>());
        Integer currentCount = tokens.get(token);
        if (currentCount != null) {
            if (currentCount < count)
                count = currentCount;

            tokens.put(token, currentCount - count);

            for (GameStateListener listener : getAllGameStateListeners())
                listener.removeTokens(card, token, count);
        }
    }

    public void setTwilight(int twilight) {
        _twilightPool = twilight;
        for (GameStateListener listener : getAllGameStateListeners())
            listener.setTwilight(_twilightPool);
    }

    public int getTwilightPool() {
        return _twilightPool;
    }

    public void startPlayerTurn(String playerId) {
        _playerOrder.setCurrentPlayer(playerId);

        for (GameStateListener listener : getAllGameStateListeners())
            listener.setCurrentPlayerId(playerId);
    }

    public boolean isCardInPlayActive(PhysicalCard card) {
        // Either it's not attached or attached to active card
        // AND is a site or fp/ring of current player or shadow of any other player
        if (card.getAttachedTo() != null)
            return isCardInPlayActive(card.getAttachedTo());

        return true;
    }

    public void startAffectingCardsForCurrentPlayer(DefaultGame game) {
        // Active non-sites are affecting
        for (PhysicalCard physicalCard : _inPlay) {
            if (isCardInPlayActive(physicalCard) && physicalCard.getBlueprint().getCardType() != CardType.SITE)
                physicalCard.startAffectingGame(game);
            else if (physicalCard.getBlueprint().getCardType() == CardType.SITE &&
                    physicalCard.getCardController() != null) {
                startAffectingControlledSite(game, physicalCard);
            }
        }

        // Stacked cards on active cards are stack-affecting
        for (List<PhysicalCard> stackedCards : _stacked.values())
            for (PhysicalCard stackedCard : stackedCards)
                if (isCardInPlayActive(stackedCard.getStackedOn()))
                    startAffectingStacked(game, stackedCard);

        for (List<PhysicalCard> discardedCards : _discards.values())
            for (PhysicalCard discardedCard : discardedCards)
                startAffectingInDiscard(game, discardedCard);
    }

    void startAffectingControlledSite(DefaultGame game, PhysicalCard physicalCard) {
        physicalCard.startAffectingGameControlledSite(game);
    }

    public void stopAffectingCardsForCurrentPlayer() {
        _inPlay.forEach(PhysicalCard::stopAffectingGame);

        for (List<PhysicalCard> stackedCards : _stacked.values())
            for (PhysicalCard stackedCard : stackedCards)
                if (isCardInPlayActive(stackedCard.getStackedOn()))
                    stopAffectingStacked(stackedCard);

        for (List<PhysicalCard> discardedCards : _discards.values())
            for (PhysicalCard discardedCard : discardedCards)
                stopAffectingInDiscard(discardedCard);
    }

    void startAffectingStacked(DefaultGame game, PhysicalCard card) {
        if (isCardAffectingGame(card))
            card.startAffectingGameStacked(game);
    }

    void stopAffectingStacked(PhysicalCard card) {
        if (isCardAffectingGame(card))
            card.stopAffectingGameStacked();
    }

    void startAffectingInDiscard(DefaultGame game, PhysicalCard card) {
        if (isCardAffectingGame(card))
            card.startAffectingGameInDiscard(game);
    }

    void stopAffectingInDiscard(PhysicalCard card) {
        if (isCardAffectingGame(card))
            card.stopAffectingGameInDiscard();
    }

    private boolean isCardAffectingGame(PhysicalCard card) {
        final Side side = card.getBlueprint().getSide();
        if (side == Side.SHADOW)
            return !getCurrentPlayerId().equals(card.getOwner());
        else if (side == Side.FREE_PEOPLE)
            return getCurrentPlayerId().equals(card.getOwner());
        else
            return false;
    }

    public void setCurrentPhase(Phase phase) {
        _currentPhase = phase;
        for (GameStateListener listener : getAllGameStateListeners())
            listener.setCurrentPhase(getPhaseString());
    }

    public Phase getCurrentPhase() {
        return _currentPhase;
    }

    public PhysicalCard getSite(int siteNumber) {
        for (PhysicalCard physicalCard : _inPlay) {
            LotroCardBlueprint blueprint = physicalCard.getBlueprint();
            if (blueprint.getCardType() == CardType.SITE && physicalCard.getSiteNumber() == siteNumber)
                return physicalCard;
        }
        return null;
    }

    public PhysicalCard getCurrentSite() {
        return getSite(getCurrentSiteNumber());
    }

    public SitesBlock getCurrentSiteBlock() {
        return getCurrentSite().getBlueprint().getSiteBlock();
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

    public void playEffectReturningResult(PhysicalCard cardPlayed) {
        sendMessage("DEBUG: playEffectReturningResult called for a default game state");
    }

    public void playerPassEffect() {}

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