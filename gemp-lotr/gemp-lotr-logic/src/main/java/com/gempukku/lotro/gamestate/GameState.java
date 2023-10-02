package com.gempukku.lotro.gamestate;

import com.gempukku.lotro.cards.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.decisions.AwaitingDecision;
import com.gempukku.lotro.game.*;
import com.gempukku.lotro.modifiers.ModifierFlag;
import com.gempukku.lotro.processes.Assignment;
import com.gempukku.lotro.processes.Skirmish;
import org.apache.log4j.Logger;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GameState {
    private static final Logger _log = Logger.getLogger(GameState.class);
    private static final int LAST_MESSAGE_STORED_COUNT = 15;
    protected PlayerOrder _playerOrder;
    protected GameFormat _format;
    protected final Map<String, Player> _players = new HashMap<>();
    protected final Map<String, List<PhysicalCardImpl>> _adventureDecks = new HashMap<>();
    protected final Map<String, List<PhysicalCardImpl>> _decks = new HashMap<>();
    protected final Map<String, List<PhysicalCardImpl>> _hands = new HashMap<>();
    protected final Map<String, List<PhysicalCardImpl>> _discards = new HashMap<>();
    private final Map<String, List<PhysicalCardImpl>> _deadPiles = new HashMap<>();
    protected final Map<String, List<PhysicalCardImpl>> _stacked = new HashMap<>();

    private final Map<String, List<PhysicalCardImpl>> _voids = new HashMap<>();
    private final Map<String, List<PhysicalCardImpl>> _voidsFromHand = new HashMap<>();
    protected final Map<String, List<PhysicalCardImpl>> _removed = new HashMap<>();

    protected final List<PhysicalCardImpl> _inPlay = new LinkedList<>();

    private final Map<Integer, PhysicalCardImpl> _allCards = new HashMap<>();

    private String _currentPlayerId;
    private Phase _currentPhase = Phase.PUT_RING_BEARER;
    private int _twilightPool;

    private int _moveCount;
    private boolean _moving;
    private boolean _fierceSkirmishes;
    private boolean _extraSkirmishes;

    private boolean _consecutiveAction;

    private final Map<String, Integer> _playerPosition = new HashMap<>();
    private final Map<String, Integer> _playerThreats = new HashMap<>();

    private final Map<LotroPhysicalCard, Map<Token, Integer>> _cardTokens = new HashMap<>();

    private final Map<String, AwaitingDecision> _playerDecisions = new HashMap<>();

    private final List<Assignment> _assignments = new LinkedList<>();
    private Skirmish _skirmish = null;

    private final Set<GameStateListener> _gameStateListeners = new HashSet<>();
    private final LinkedList<String> _lastMessages = new LinkedList<>();

    private int _nextCardId = 0;
    protected Map<String, List<String>> _cards;

    private int nextCardId() {
        return _nextCardId++;
    }

    public GameState(Set<String> players, Map<String, CardDeck> decks, CardBlueprintLibrary library, GameFormat format) {
        _format = format;
        try {
            addPlayerCards(players, decks, library);
        } catch (CardNotFoundException e) {
            throw new RuntimeException("Invalid blueprint ID found in card deck while creating game state");
        }
        for (String playerId : players) {
            _adventureDecks.put(playerId, new LinkedList<>());
            _decks.put(playerId, new LinkedList<>());
            _hands.put(playerId, new LinkedList<>());
            _voids.put(playerId, new LinkedList<>());
            _voidsFromHand.put(playerId, new LinkedList<>());
            _removed.put(playerId, new LinkedList<>());
            _discards.put(playerId, new LinkedList<>());
            _deadPiles.put(playerId, new LinkedList<>());
            _stacked.put(playerId, new LinkedList<>());

            for(var site : getAdventureDeck(playerId)) {
                for (GameStateListener listener : getAllGameStateListeners()) {
                    listener.cardCreated(site);
                }
            }
        }
    }

    public void init(PlayerOrder playerOrder, String firstPlayer) {
        _playerOrder = playerOrder;
        _currentPlayerId = firstPlayer;
        for (String player : playerOrder.getAllPlayers()) {
            _players.put(player, new Player(player));
            _playerThreats.put(player, 0);
        }
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
            for(var card : getDeck(playerId)) {
                for (GameStateListener listener : getAllGameStateListeners()) {
                    listener.cardCreated(card, true);
                }
            }
        }
    }

    public boolean isMoving() {
        return _moving;
    }

    public void setMoving(boolean moving) {
        _moving = moving;
    }

    void addPlayerCards(Set<String> players, Map<String, CardDeck> decks, CardBlueprintLibrary library) throws CardNotFoundException {
        /*
            // TODO: Nothing implemented here, but this should also never get called
         */
    }

    public LotroPhysicalCard createPhysicalCard(String ownerPlayerId, CardBlueprintLibrary library, String blueprintId) throws CardNotFoundException {
        return createPhysicalCardImpl(ownerPlayerId, library, blueprintId);
    }

    protected PhysicalCardImpl createPhysicalCardImpl(String playerId, CardBlueprintLibrary library, String blueprintId) throws CardNotFoundException {
        LotroCardBlueprint card = library.getLotroCardBlueprint(blueprintId);

        int cardId = nextCardId();
        PhysicalCardImpl result = new PhysicalCardImpl(cardId, blueprintId, playerId, card);

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

    private String getPhaseString() {
        if (isFierceSkirmishes())
            return "Fierce " + _currentPhase.getHumanReadable();
        if (isExtraSkirmishes())
            return "Extra " + _currentPhase.getHumanReadable();
        return _currentPhase.getHumanReadable();
    }

    private void sendStateToPlayer(String playerId, GameStateListener listener, GameStats gameStats) {
        if (_playerOrder != null) {
            listener.initializeBoard(_playerOrder.getAllPlayers(), _format.discardPileIsPublic());
            if (_currentPlayerId != null)
                listener.setCurrentPlayerId(_currentPlayerId);
            if (_currentPhase != null)
                listener.setCurrentPhase(getPhaseString());
            listener.setTwilight(_twilightPool);
            for (Map.Entry<String, Integer> stringIntegerEntry : _playerPosition.entrySet())
                listener.setPlayerPosition(stringIntegerEntry.getKey(), stringIntegerEntry.getValue());

            Set<LotroPhysicalCard> cardsLeftToSent = new LinkedHashSet<>(_inPlay);
            Set<LotroPhysicalCard> sentCardsFromPlay = new HashSet<>();

            int cardsToSendAtLoopStart;
            do {
                cardsToSendAtLoopStart = cardsLeftToSent.size();
                Iterator<LotroPhysicalCard> cardIterator = cardsLeftToSent.iterator();
                while (cardIterator.hasNext()) {
                    LotroPhysicalCard physicalCard = cardIterator.next();
                    LotroPhysicalCard attachedTo = physicalCard.getAttachedTo();
                    if (attachedTo == null || sentCardsFromPlay.contains(attachedTo)) {
                        listener.cardCreated(physicalCard);
                        sentCardsFromPlay.add(physicalCard);

                        cardIterator.remove();
                    }
                }
            } while (cardsToSendAtLoopStart != cardsLeftToSent.size() && cardsLeftToSent.size() > 0);

            // Finally the stacked ones
            for (List<PhysicalCardImpl> physicalCards : _stacked.values())
                for (PhysicalCardImpl physicalCard : physicalCards)
                    listener.cardCreated(physicalCard);

            List<PhysicalCardImpl> hand = _hands.get(playerId);
            if (hand != null) {
                for (PhysicalCardImpl physicalCard : hand)
                    listener.cardCreated(physicalCard);
            }

            for (List<PhysicalCardImpl> physicalCards : _deadPiles.values())
                for (PhysicalCardImpl physicalCard : physicalCards)
                    listener.cardCreated(physicalCard);

            List<PhysicalCardImpl> discard = _discards.get(playerId);
            if (discard != null) {
                for (PhysicalCardImpl physicalCard : discard)
                    listener.cardCreated(physicalCard);
            }

            List<PhysicalCardImpl> adventureDeck = _adventureDecks.get(playerId);
            if (adventureDeck != null) {
                for (PhysicalCardImpl physicalCard : adventureDeck)
                    listener.cardCreated(physicalCard);
            }

            for (Assignment assignment : _assignments)
                listener.addAssignment(assignment.getFellowshipCharacter(), assignment.getShadowCharacters());

            if (_skirmish != null)
                listener.startSkirmish(_skirmish.getFellowshipCharacter(), _skirmish.getShadowCharacters());

            for (Map.Entry<LotroPhysicalCard, Map<Token, Integer>> physicalCardMapEntry : _cardTokens.entrySet()) {
                LotroPhysicalCard card = physicalCardMapEntry.getKey();
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

    public void transferCard(LotroPhysicalCard card, LotroPhysicalCard transferTo) {
        if (card.getZone() != Zone.ATTACHED)
            ((PhysicalCardImpl) card).setZone(Zone.ATTACHED);

        ((PhysicalCardImpl) card).attachTo((PhysicalCardImpl) transferTo);
        for (GameStateListener listener : getAllGameStateListeners())
            listener.cardMoved(card);
    }

    public void takeControlOfCard(String playerId, DefaultGame game, LotroPhysicalCard card, Zone zone) {
        ((PhysicalCardImpl) card).setCardController(playerId);
        ((PhysicalCardImpl) card).setZone(zone);
        if (card.getBlueprint().getCardType() == CardType.SITE)
            startAffectingControlledSite(game, card);
        for (GameStateListener listener : getAllGameStateListeners())
            listener.cardMoved(card);
    }

    public void loseControlOfCard(LotroPhysicalCard card, Zone zone) {
        ((PhysicalCardImpl) card).setCardController(null);
        ((PhysicalCardImpl) card).setZone(zone);
        for (GameStateListener listener : getAllGameStateListeners())
            listener.cardMoved(card);
    }

    public void attachCard(DefaultGame game, LotroPhysicalCard card, LotroPhysicalCard attachTo) throws InvalidParameterException {
        if(card == attachTo)
            throw new InvalidParameterException("Cannot attach card to itself!");

        ((PhysicalCardImpl) card).attachTo((PhysicalCardImpl) attachTo);
        addCardToZone(game, card, Zone.ATTACHED);
    }

    public void stackCard(DefaultGame game, LotroPhysicalCard card, LotroPhysicalCard stackOn) throws InvalidParameterException {
        if(card == stackOn)
            throw new InvalidParameterException("Cannot stack card on itself!");

        ((PhysicalCardImpl) card).stackOn((PhysicalCardImpl) stackOn);
        addCardToZone(game, card, Zone.STACKED);
    }

    public void cardAffectsCard(String playerPerforming, LotroPhysicalCard card, Collection<LotroPhysicalCard> affectedCards) {
        for (GameStateListener listener : getAllGameStateListeners())
            listener.cardAffectedByCard(playerPerforming, card, affectedCards);
    }

    public void eventPlayed(LotroPhysicalCard card) {
        for (GameStateListener listener : getAllGameStateListeners())
            listener.eventPlayed(card);
    }

    public void activatedCard(String playerPerforming, LotroPhysicalCard card) {
        for (GameStateListener listener : getAllGameStateListeners())
            listener.cardActivated(playerPerforming, card);
    }

    public LotroPhysicalCard getRingBearer(String playerId) {
        return null;
    }

    public List<PhysicalCardImpl> getZoneCards(String playerId, Zone zone) {
        if (zone == Zone.DECK)
            return _decks.get(playerId);
        else if (zone == Zone.ADVENTURE_DECK)
            return _adventureDecks.get(playerId);
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

    public void removeFromSkirmish(LotroPhysicalCard card) {
        removeFromSkirmish(card, true);
    }

    public void replaceInSkirmish(LotroPhysicalCard card) {
        _skirmish.setFellowshipCharacter(card);
        for (GameStateListener gameStateListener : getAllGameStateListeners()) {
            gameStateListener.finishSkirmish();
            gameStateListener.startSkirmish(_skirmish.getFellowshipCharacter(), _skirmish.getShadowCharacters());
        }
    }

    private void removeFromSkirmish(LotroPhysicalCard card, boolean notify) {
        if (_skirmish != null) {
            if (_skirmish.getFellowshipCharacter() == card) {
                _skirmish.setFellowshipCharacter(null);
                _skirmish.addRemovedFromSkirmish(card);
                if (notify)
                    for (GameStateListener listener : getAllGameStateListeners())
                        listener.removeFromSkirmish(card);
            }
            if (_skirmish.getShadowCharacters().remove(card)) {
                _skirmish.addRemovedFromSkirmish(card);
                if (notify)
                    for (GameStateListener listener : getAllGameStateListeners())
                        listener.removeFromSkirmish(card);
            }
        }
    }

    public void removeCardsFromZone(String playerPerforming, Collection<LotroPhysicalCard> cards) {
        for (LotroPhysicalCard card : cards) {
            List<PhysicalCardImpl> zoneCards = getZoneCards(card.getOwner(), card.getZone());
            if (!zoneCards.contains(card))
                _log.error("Card was not found in the expected zone");
        }

        for (LotroPhysicalCard card : cards) {
            Zone zone = card.getZone();

            if (zone.isInPlay())
                if (card.getBlueprint().getCardType() != CardType.SITE ||
                        (getCurrentPhase() != Phase.PLAY_STARTING_FELLOWSHIP && getCurrentSite() == card))
                    stopAffecting(card);

            if (zone == Zone.STACKED)
                stopAffectingStacked(card);
            else if (zone == Zone.DISCARD)
                stopAffectingInDiscard(card);

            List<PhysicalCardImpl> zoneCards = getZoneCards(card.getOwner(), zone);
            zoneCards.remove(card);

            if (zone.isInPlay())
                _inPlay.remove(card);
            if (zone == Zone.ATTACHED)
                ((PhysicalCardImpl) card).attachTo(null);

            if (zone == Zone.STACKED)
                ((PhysicalCardImpl) card).stackOn(null);

            //If this is reset, then there is no way for self-discounting effects (which are evaluated while in the void)
            // to have any sort of permanent effect once the card is in play.
            if(zone != Zone.VOID_FROM_HAND && zone != Zone.VOID)
                card.setWhileInZoneData(null);
        }

        for (GameStateListener listener : getAllGameStateListeners())
            listener.cardsRemoved(playerPerforming, cards);

        for (LotroPhysicalCard card : cards) {
            ((PhysicalCardImpl) card).setZone(null);
        }
    }

    public void addCardToZone(DefaultGame game, LotroPhysicalCard card, Zone zone) {
        addCardToZone(game, card, zone, true);
    }

    public void addCardToZone(DefaultGame game, LotroPhysicalCard card, Zone zone, boolean end) {
        if (zone == Zone.DISCARD && game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.REMOVE_CARDS_GOING_TO_DISCARD))
            zone = Zone.REMOVED;

        if (zone.isInPlay()) {
            assignNewCardId(card);
            _inPlay.add((PhysicalCardImpl) card);
        }

        List<PhysicalCardImpl> zoneCards = getZoneCards(card.getOwner(), zone);
        if (end)
            zoneCards.add((PhysicalCardImpl) card);
        else
            zoneCards.add(0, (PhysicalCardImpl) card);

        if (card.getZone() != null)
            _log.error("Card was in " + card.getZone() + " when tried to add to zone: " + zone);

        ((PhysicalCardImpl) card).setZone(zone);

        if (zone == Zone.ADVENTURE_PATH) {
            for (GameStateListener listener : getAllGameStateListeners())
                listener.setSite(card);
        } else {
            for (GameStateListener listener : getAllGameStateListeners())
                listener.cardCreated(card);
        }

//        if (_currentPhase.isCardsAffectGame()) {
        if (zone.isInPlay())
            startAffecting(game, card);

        if (zone == Zone.STACKED)
            startAffectingStacked(game, card);
        else if (zone == Zone.DISCARD)
            startAffectingInDiscard(game, card);
    }

    void assignNewCardId(LotroPhysicalCard card) {
        _allCards.remove(card.getCardId());
        int newCardId = nextCardId();
        ((PhysicalCardImpl) card).setCardId(newCardId);
        _allCards.put(newCardId, ((PhysicalCardImpl) card));
    }

    private void removeAllTokens(LotroPhysicalCard card) {
        Map<Token, Integer> map = _cardTokens.get(card);
        if (map != null) {
            for (Map.Entry<Token, Integer> tokenIntegerEntry : map.entrySet())
                if (tokenIntegerEntry.getValue() > 0)
                    for (GameStateListener listener : getAllGameStateListeners())
                        listener.removeTokens(card, tokenIntegerEntry.getKey(), tokenIntegerEntry.getValue());

            map.clear();
        }
    }

    public void shuffleCardsIntoDeck(Collection<? extends LotroPhysicalCard> cards, String playerId) {
        List<PhysicalCardImpl> zoneCards = _decks.get(playerId);

        for (LotroPhysicalCard card : cards) {
            zoneCards.add((PhysicalCardImpl) card);

            ((PhysicalCardImpl) card).setZone(Zone.DECK);
        }

        shuffleDeck(playerId);
    }

    public void putCardOnBottomOfDeck(LotroPhysicalCard card) {
        addCardToZone(null, card, Zone.DECK, true);
    }

    public void putCardOnTopOfDeck(LotroPhysicalCard card) {
        addCardToZone(null, card, Zone.DECK, false);
    }

    public void iterateActiveCards(PhysicalCardVisitor physicalCardVisitor) {
        for (PhysicalCardImpl physicalCard : _inPlay) {
            if (isCardInPlayActive(physicalCard))
                if (physicalCardVisitor.visitPhysicalCard(physicalCard))
                    return;
        }

    }

    public LotroPhysicalCard findCardById(int cardId) {
        return _allCards.get(cardId);
    }

    public Iterable<? extends LotroPhysicalCard> getAllCards() {
        return Collections.unmodifiableCollection(_allCards.values());
    }

    public List<? extends LotroPhysicalCard> getHand(String playerId) {
        return Collections.unmodifiableList(_hands.get(playerId));
    }

    public List<? extends LotroPhysicalCard> getVoidFromHand(String playerId) {
        return Collections.unmodifiableList(_voidsFromHand.get(playerId));
    }

    public List<? extends LotroPhysicalCard> getRemoved(String playerId) {
        return Collections.unmodifiableList(_removed.get(playerId));
    }

    public List<? extends LotroPhysicalCard> getDeck(String playerId) {
        return Collections.unmodifiableList(_decks.get(playerId));
    }

    public List<? extends LotroPhysicalCard> getDiscard(String playerId) {
        return Collections.unmodifiableList(_discards.get(playerId));
    }

    public List<? extends LotroPhysicalCard> getDeadPile(String playerId) {
        return Collections.unmodifiableList(_deadPiles.get(playerId));
    }

    public List<? extends LotroPhysicalCard> getAdventureDeck(String playerId) {
        return Collections.unmodifiableList(_adventureDecks.get(playerId));
    }

    public List<? extends LotroPhysicalCard> getInPlay() {
        return Collections.unmodifiableList(_inPlay);
    }

    public List<? extends LotroPhysicalCard> getStacked(String playerId) {
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

    public void setPlayerPosition(String playerId, int i) {
        _playerPosition.put(playerId, i);
        for (GameStateListener listener : getAllGameStateListeners())
            listener.setPlayerPosition(playerId, i);
    }

    public void addThreats(String playerId, int count) {
        _playerThreats.put(playerId, _playerThreats.get(playerId) + count);
    }

    public void removeThreats(String playerId, int count) {
        final int oldThreats = _playerThreats.get(playerId);
        count = Math.min(count, oldThreats);
        _playerThreats.put(playerId, oldThreats - count);
    }

    public void movePlayerToNextSite(DefaultGame game) {
        final String currentPlayerId = getCurrentPlayerId();
        final int oldPlayerPosition = getPlayerPosition(currentPlayerId);
        stopAffecting(getCurrentSite());
        setPlayerPosition(currentPlayerId, oldPlayerPosition + 1);
        increaseMoveCount();
        startAffecting(game, getCurrentSite());
    }

    public int getPlayerPosition(String playerId) {
        return _playerPosition.getOrDefault(playerId, 0);
    }

    public Map<Token, Integer> getTokens(LotroPhysicalCard card) {
        Map<Token, Integer> map = _cardTokens.get(card);
        if (map == null)
            return Collections.emptyMap();
        return Collections.unmodifiableMap(map);
    }

    public int getTokenCount(LotroPhysicalCard physicalCard, Token token) {
        Map<Token, Integer> tokens = _cardTokens.get(physicalCard);
        if (tokens == null)
            return 0;
        Integer count = tokens.get(token);
        if (count == null)
            return 0;
        return count;
    }

    public List<LotroPhysicalCard> getAttachedCards(LotroPhysicalCard card) {
        List<LotroPhysicalCard> result = new LinkedList<>();
        for (PhysicalCardImpl physicalCard : _inPlay) {
            if (physicalCard.getAttachedTo() != null && physicalCard.getAttachedTo() == card)
                result.add(physicalCard);
        }
        return result;
    }

    public List<LotroPhysicalCard> getStackedCards(LotroPhysicalCard card) {
        List<LotroPhysicalCard> result = new LinkedList<>();
        for (List<PhysicalCardImpl> physicalCardList : _stacked.values()) {
            for (PhysicalCardImpl physicalCard : physicalCardList) {
                if (physicalCard.getStackedOn() == card)
                    result.add(physicalCard);
            }
        }
        return result;
    }

    public int getWounds(LotroPhysicalCard physicalCard) {
        return getTokenCount(physicalCard, Token.WOUND);
    }

    public int getBurdens() {
        return 0;
    }

    public int getThreats() {
        return _playerThreats.get(getCurrentPlayerId());
    }

    public void addWound(LotroPhysicalCard card) {
        addTokens(card, Token.WOUND, 1);
    }

    public void removeWound(LotroPhysicalCard card) {
        removeTokens(card, Token.WOUND, 1);
    }

    public void addTokens(LotroPhysicalCard card, Token token, int count) {
        Map<Token, Integer> tokens = _cardTokens.computeIfAbsent(card, k -> new HashMap<>());
        tokens.merge(token, count, Integer::sum);

        for (GameStateListener listener : getAllGameStateListeners())
            listener.addTokens(card, token, count);
    }

    public void removeTokens(LotroPhysicalCard card, Token token, int count) {
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

    public boolean isExtraSkirmishes() {
        return _extraSkirmishes;
    }

    public void setExtraSkirmishes(boolean extraSkirmishes) {
        _extraSkirmishes = extraSkirmishes;
    }

    public boolean isFierceSkirmishes() {
        return _fierceSkirmishes;
    }

    public boolean isCardInPlayActive(LotroPhysicalCard card) {
        // Either it's not attached or attached to active card
        // AND is a site or fp/ring of current player or shadow of any other player
        if (card.getAttachedTo() != null)
            return isCardInPlayActive(card.getAttachedTo());

        return true;
    }

    public void startAffectingCardsForCurrentPlayer(DefaultGame game) {
        // Active non-sites are affecting
        for (PhysicalCardImpl physicalCard : _inPlay) {
            if (isCardInPlayActive(physicalCard) && physicalCard.getBlueprint().getCardType() != CardType.SITE)
                startAffecting(game, physicalCard);
            else if (physicalCard.getBlueprint().getCardType() == CardType.SITE &&
                    physicalCard.getCardController() != null) {
                startAffectingControlledSite(game, physicalCard);
            }
        }

        // Stacked cards on active cards are stack-affecting
        for (List<PhysicalCardImpl> stackedCards : _stacked.values())
            for (PhysicalCardImpl stackedCard : stackedCards)
                if (isCardInPlayActive(stackedCard.getStackedOn()))
                    startAffectingStacked(game, stackedCard);

        for (List<PhysicalCardImpl> discardedCards : _discards.values())
            for (PhysicalCardImpl discardedCard : discardedCards)
                startAffectingInDiscard(game, discardedCard);
    }

    void startAffectingControlledSite(DefaultGame game, LotroPhysicalCard physicalCard) {
        ((PhysicalCardImpl) physicalCard).startAffectingGameControlledSite(game);
    }

    public void stopAffectingCardsForCurrentPlayer() {
        for (PhysicalCardImpl physicalCard : _inPlay) {
            stopAffecting(physicalCard);
        }

        for (List<PhysicalCardImpl> stackedCards : _stacked.values())
            for (PhysicalCardImpl stackedCard : stackedCards)
                if (isCardInPlayActive(stackedCard.getStackedOn()))
                    stopAffectingStacked(stackedCard);

        for (List<PhysicalCardImpl> discardedCards : _discards.values())
            for (PhysicalCardImpl discardedCard : discardedCards)
                stopAffectingInDiscard(discardedCard);
    }

    private void stopAffectingControlledSite(LotroPhysicalCard physicalCard) {
        ((PhysicalCardImpl) physicalCard).stopAffectingGameControlledSite();
    }

    void startAffecting(DefaultGame game, LotroPhysicalCard card) {
        ((PhysicalCardImpl) card).startAffectingGame(game);
    }

    void stopAffecting(LotroPhysicalCard card) {
        ((PhysicalCardImpl) card).stopAffectingGame();
    }

    void startAffectingStacked(DefaultGame game, LotroPhysicalCard card) {
        if (isCardAffectingGame(card))
            ((PhysicalCardImpl) card).startAffectingGameStacked(game);
    }

    void stopAffectingStacked(LotroPhysicalCard card) {
        if (isCardAffectingGame(card))
            ((PhysicalCardImpl) card).stopAffectingGameStacked();
    }

    void startAffectingInDiscard(DefaultGame game, LotroPhysicalCard card) {
        if (isCardAffectingGame(card))
            ((PhysicalCardImpl) card).startAffectingGameInDiscard(game);
    }

    void stopAffectingInDiscard(LotroPhysicalCard card) {
        if (isCardAffectingGame(card))
            ((PhysicalCardImpl) card).stopAffectingGameInDiscard();
    }

    private boolean isCardAffectingGame(LotroPhysicalCard card) {
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

    public LotroPhysicalCard getSite(int siteNumber) {
        for (PhysicalCardImpl physicalCard : _inPlay) {
            LotroCardBlueprint blueprint = physicalCard.getBlueprint();
            if (blueprint.getCardType() == CardType.SITE && physicalCard.getSiteNumber() == siteNumber)
                return physicalCard;
        }
        return null;
    }

    public LotroPhysicalCard getCurrentSite() {
        return getSite(getCurrentSiteNumber());
    }

    public SitesBlock getCurrentSiteBlock() {
        return getCurrentSite().getBlueprint().getSiteBlock();
    }

    public void increaseMoveCount() {
        _moveCount++;
    }

    public int getMoveCount() {
        return _moveCount;
    }

    public void addTwilight(int twilight) {
        setTwilight(_twilightPool + Math.max(0, twilight));
    }

    public void removeTwilight(int twilight) {
        setTwilight(_twilightPool - Math.min(Math.max(0, twilight), _twilightPool));
    }

    public void assignToSkirmishes(LotroPhysicalCard fp, Set<LotroPhysicalCard> minions) {
        removeFromSkirmish(fp);
        for (LotroPhysicalCard minion : minions) {
            removeFromSkirmish(minion);

            for (Assignment assignment : new LinkedList<>(_assignments)) {
                if (assignment.getShadowCharacters().remove(minion))
                    if (assignment.getShadowCharacters().size() == 0)
                        removeAssignment(assignment);
            }
        }

        Assignment assignment = findAssignment(fp);
        if (assignment != null)
            assignment.getShadowCharacters().addAll(minions);
        else
            _assignments.add(new Assignment(fp, new HashSet<>(minions)));

        for (GameStateListener listener : getAllGameStateListeners())
            listener.addAssignment(fp, minions);
    }

    public void removeAssignment(Assignment assignment) {
        _assignments.remove(assignment);
        for (GameStateListener listener : getAllGameStateListeners())
            listener.removeAssignment(assignment.getFellowshipCharacter());
    }

    public List<Assignment> getAssignments() {
        return _assignments;
    }

    private Assignment findAssignment(LotroPhysicalCard fp) {
        for (Assignment assignment : _assignments)
            if (assignment.getFellowshipCharacter() == fp)
                return assignment;
        return null;
    }

    public Skirmish getSkirmish() {
        return _skirmish;
    }

    public void finishSkirmish() {
        if (_skirmish != null) {
            _skirmish = null;
            for (GameStateListener listener : getAllGameStateListeners())
                listener.finishSkirmish();
        }
    }

    public LotroPhysicalCard removeTopDeckCard(String player) {
        List<PhysicalCardImpl> deck = _decks.get(player);
        if (deck.size() > 0) {
            final LotroPhysicalCard topDeckCard = deck.get(0);
            removeCardsFromZone(null, Collections.singleton(topDeckCard));
            return topDeckCard;
        } else {
            return null;
        }
    }

    public LotroPhysicalCard removeBottomDeckCard(String player) {
        List<PhysicalCardImpl> deck = _decks.get(player);
        if (deck.size() > 0) {
            final LotroPhysicalCard topDeckCard = deck.get(deck.size() - 1);
            removeCardsFromZone(null, Collections.singleton(topDeckCard));
            return topDeckCard;
        } else {
            return null;
        }
    }

    public LotroPhysicalCard removeTopCardFromZone(String player, Zone zone) {
        // Assumes pulling from final element of deck. Logic works for Tribbles play pile deck as of 8/15/23
        List<PhysicalCardImpl> deck = getZoneCards(player, zone);
        if (deck.size() > 0) {
            final LotroPhysicalCard topDeckCard = deck.get(deck.size() - 1);
            removeCardsFromZone(null, Collections.singleton(topDeckCard));
            return topDeckCard;
        } else {
            return null;
        }
    }


    public void playerDrawsCard(String player) {
        List<PhysicalCardImpl> deck = _decks.get(player);
        if (deck.size() > 0) {
            LotroPhysicalCard card = deck.get(0);
            removeCardsFromZone(null, Collections.singleton(card));
            addCardToZone(null, card, Zone.HAND);
        }
    }

    public void shuffleDeck(String player) {
        List<PhysicalCardImpl> deck = _decks.get(player);
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

    public void playEffectReturningResult(LotroPhysicalCard cardPlayed) {
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
        List<LotroPhysicalCard> hand = new LinkedList<>(getHand(playerId));
        removeCardsFromZone(playerId, hand);
        for (LotroPhysicalCard card : hand) {
            addCardToZone(game, card, Zone.DISCARD);
        }
    }
}