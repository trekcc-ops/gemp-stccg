package com.gempukku.lotro.gamestate;

import com.gempukku.lotro.cards.*;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.decisions.AwaitingDecision;
import com.gempukku.lotro.game.*;
import com.gempukku.lotro.modifiers.ModifierFlag;
import org.apache.log4j.Logger;

import java.security.InvalidParameterException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class TribblesGameState extends GameState {
    private static final Logger _log = Logger.getLogger(TribblesGameState.class);
    private static final int LAST_MESSAGE_STORED_COUNT = 30;
    private PlayerOrder _playerOrder;
    private LotroFormat _format;
    private final Map<String, List<PhysicalCardImpl>> _playPiles = new HashMap<>();
    private final Map<String, List<PhysicalCardImpl>> _adventureDecks = new HashMap<>();
    private final Map<String, List<PhysicalCardImpl>> _decks = new HashMap<>();
    private final Map<String, List<PhysicalCardImpl>> _hands = new HashMap<>();
    private final Map<String, List<PhysicalCardImpl>> _discards = new HashMap<>();
    private final Map<String, List<PhysicalCardImpl>> _stacked = new HashMap<>();
    private final Map<String, List<PhysicalCardImpl>> _removed = new HashMap<>();
    private final List<PhysicalCardImpl> _inPlay = new LinkedList<>();
    private final Map<Integer, PhysicalCardImpl> _allCards = new HashMap<>();
    private Phase _currentPhase = Phase.PUT_RING_BEARER;
    private boolean _consecutiveAction;
    private final Map<String, AwaitingDecision> _playerDecisions = new HashMap<>();

    private final Set<GameStateListener> _gameStateListeners = new HashSet<>();
    private final LinkedList<String> _lastMessages = new LinkedList<>();
    private int _nextCardId = 0;
    private int _nextTribbleInSequence;
    private int _lastTribblePlayed;
    private boolean _chainBroken;
    private int _currentRound = 0;
    private int nextCardId() {
        return _nextCardId++;
    }

    public void init(PlayerOrder playerOrder, String firstPlayer, Map<String, List<String>> cards,
                     CardBlueprintLibrary library, LotroFormat format) {
        _playerOrder = playerOrder;
        setCurrentPlayerId(firstPlayer);
        _format = format;

        for (String player : playerOrder.getAllPlayers()) {
            _players.put(player, new Player(player));
        }

        _nextTribbleInSequence = 1; // TODO: Replace with setNextTribble statement
        _chainBroken = false;

        for (Map.Entry<String, List<String>> stringListEntry : cards.entrySet()) {
            String playerId = stringListEntry.getKey();
            List<String> decks = stringListEntry.getValue();

            _adventureDecks.put(playerId, new LinkedList<>());
            _decks.put(playerId, new LinkedList<>());
            _hands.put(playerId, new LinkedList<>());
            _removed.put(playerId, new LinkedList<>());
            _discards.put(playerId, new LinkedList<>());
            _stacked.put(playerId, new LinkedList<>());
            _playPiles.put(playerId, new LinkedList<>());

            addPlayerCards(playerId, decks, library);
        }

        for (GameStateListener listener : getAllGameStateListeners()) {
            listener.initializeBoard(playerOrder.getAllPlayers(), format.discardPileIsPublic());
        }

        //This needs done after the Player Order initialization has been issued, or else the player
        // adventure deck areas don't exist.
        for (String playerId : playerOrder.getAllPlayers()) {
            for(var site : getAdventureDeck(playerId)) {
                for (GameStateListener listener : getAllGameStateListeners()) {
                    listener.cardCreated(site);
                }
            }
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

    private void addPlayerCards(String playerId, List<String> cards, CardBlueprintLibrary library) {
        for (String blueprintId : cards) {
            try {
                PhysicalCardImpl physicalCard = createPhysicalCardImpl(playerId, library, blueprintId);
                if (physicalCard.getBlueprint().getCardType() == CardType.SITE) {
                    physicalCard.setZone(Zone.ADVENTURE_DECK);
                    _adventureDecks.get(playerId).add(physicalCard);
                } else {
                    physicalCard.setZone(Zone.DECK);
                    _decks.get(playerId).add(physicalCard);
                }
            } catch (CardNotFoundException exp) {
                // Ignore the card
            }
        }
    }

    public LotroPhysicalCard createPhysicalCard(String ownerPlayerId, CardBlueprintLibrary library, String blueprintId) throws CardNotFoundException {
        return createPhysicalCardImpl(ownerPlayerId, library, blueprintId);
    }

    private PhysicalCardImpl createPhysicalCardImpl(String playerId, CardBlueprintLibrary library, String blueprintId)
            throws CardNotFoundException {
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

    private Collection<GameStateListener> getAllGameStateListeners() {
        return Collections.unmodifiableSet(_gameStateListeners);
    }

    private String getPhaseString() {
        return _currentPhase.getHumanReadable();
    }

    private void sendStateToPlayer(String playerId, GameStateListener listener, GameStats gameStats) {
        if (_playerOrder != null) {
            listener.initializeBoard(_playerOrder.getAllPlayers(), _format.discardPileIsPublic());
            if (getCurrentPlayerId() != null)
                listener.setCurrentPlayerId(getCurrentPlayerId());
            if (_currentPhase != null)
                listener.setCurrentPhase(getPhaseString());
            for (Player player : _players.values()) {
                listener.setPlayerDecked(player.getPlayerId(), player.getDecked());
                listener.setPlayerScore(player.getPlayerId(), player.getScore());
            }

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

            List<PhysicalCardImpl> playPile = _playPiles.get(playerId);
            if (playPile != null) {
                for (PhysicalCardImpl physicalCard : playPile)
                    listener.cardCreated(physicalCard);
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

    public List<PhysicalCardImpl> getZoneCards(String playerId, Zone zone) {
        if (zone == Zone.DECK)
            return _decks.get(playerId);
        else if (zone == Zone.PLAY_PILE)
            return _playPiles.get(playerId);
        else if (zone == Zone.ADVENTURE_DECK)
            return _adventureDecks.get(playerId);
        else if (zone == Zone.DISCARD)
            return _discards.get(playerId);
        else if (zone == Zone.HAND)
            return _hands.get(playerId);
        else if (zone == Zone.REMOVED)
            return _removed.get(playerId);
        else if (zone == Zone.STACKED)
            return _stacked.get(playerId);
        else // This should never be accessed
            return _inPlay;
    }

    public void removeCardsFromZone(String playerPerforming, Collection<LotroPhysicalCard> cards) {
        for (LotroPhysicalCard card : cards) {
            List<PhysicalCardImpl> zoneCards = getZoneCards(card.getOwner(), card.getZone());
            if (!zoneCards.contains(card))
                _log.error("Card was not found in the expected zone");
        }

        for (LotroPhysicalCard card : cards) {
            Zone zone = card.getZone();

            if (zone.isInPlay()) {
                if (card.getBlueprint().getCardType() != CardType.SITE || (getCurrentPhase() != Phase.PLAY_STARTING_FELLOWSHIP))
                    stopAffecting(card);
            }

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

        if (_currentPhase.isCardsAffectGame()) {
            if (zone.isInPlay())
                if (card.getBlueprint().getCardType() != CardType.SITE || (getCurrentPhase() != Phase.PLAY_STARTING_FELLOWSHIP))
                    startAffecting(game, card);

            if (zone == Zone.STACKED)
                startAffectingStacked(game, card);
            else if (zone == Zone.DISCARD)
                startAffectingInDiscard(game, card);
        }
    }

    private void assignNewCardId(LotroPhysicalCard card) {
        _allCards.remove(card.getCardId());
        int newCardId = nextCardId();
        ((PhysicalCardImpl) card).setCardId(newCardId);
        _allCards.put(newCardId, ((PhysicalCardImpl) card));
    }

    public void shuffleCardsIntoDeck(Collection<? extends LotroPhysicalCard> cards, String playerId) {
        List<PhysicalCardImpl> zoneCards = _decks.get(playerId);

        for (LotroPhysicalCard card : cards) {
            zoneCards.add((PhysicalCardImpl) card);

            ((PhysicalCardImpl) card).setZone(Zone.DECK);
        }

        shuffleDeck(playerId);
    }

    public void shufflePlayPileIntoDeck(TribblesGame game, String playerId) {
        List<LotroPhysicalCard> playPile = new LinkedList<>(getPlayPile(playerId));
        removeCardsFromZone(playerId, playPile);
        for (LotroPhysicalCard card : playPile) {
            addCardToZone(game, card, Zone.DECK);
        }
        shuffleDeck(playerId);
    }

    public void discardHand(TribblesGame game, String playerId) {
        List<LotroPhysicalCard> hand = new LinkedList<>(getHand(playerId));
        removeCardsFromZone(playerId, hand);
        for (LotroPhysicalCard card : hand) {
            addCardToZone(game, card, Zone.DISCARD);
        }
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

    public List<? extends LotroPhysicalCard> getRemoved(String playerId) {
        return Collections.unmodifiableList(_removed.get(playerId));
    }

    public List<? extends LotroPhysicalCard> getDeck(String playerId) {
        return Collections.unmodifiableList(_decks.get(playerId));
    }

    public List<? extends LotroPhysicalCard> getDiscard(String playerId) {
        return Collections.unmodifiableList(_discards.get(playerId));
    }

    public List<? extends LotroPhysicalCard> getAdventureDeck(String playerId) {
        return Collections.unmodifiableList(_adventureDecks.get(playerId));
    }

    public List<LotroPhysicalCard> getPlayPile(String playerId) {
        return Collections.unmodifiableList(_playPiles.get(playerId));
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

    public void setPlayerDecked(String playerId, boolean bool) {
        _players.get(playerId).setDecked(bool);
        for (GameStateListener listener : getAllGameStateListeners())
            listener.setPlayerDecked(playerId, bool);
    }

    public boolean getPlayerDecked(String playerId) {
        return _players.get(playerId).getDecked();
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

    public void startPlayerTurn(String playerId) {
        _playerOrder.setCurrentPlayer(playerId);

        for (GameStateListener listener : getAllGameStateListeners())
            listener.setCurrentPlayerId(playerId);
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

    private void startAffectingControlledSite(DefaultGame game, LotroPhysicalCard physicalCard) {
        ((PhysicalCardImpl) physicalCard).startAffectingGameControlledSite(game);
    }

    public void stopAffectingCardsForCurrentPlayer() {
        for (PhysicalCardImpl physicalCard : _inPlay) {
            if (isCardInPlayActive(physicalCard) && physicalCard.getBlueprint().getCardType() != CardType.SITE)
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

    private void startAffecting(DefaultGame game, LotroPhysicalCard card) {
        ((PhysicalCardImpl) card).startAffectingGame(game);
    }

    private void stopAffecting(LotroPhysicalCard card) {
        ((PhysicalCardImpl) card).stopAffectingGame();
    }

    private void startAffectingStacked(DefaultGame game, LotroPhysicalCard card) {
        if (isCardAffectingGame(card))
            ((PhysicalCardImpl) card).startAffectingGameStacked(game);
    }

    private void stopAffectingStacked(LotroPhysicalCard card) {
        if (isCardAffectingGame(card))
            ((PhysicalCardImpl) card).stopAffectingGameStacked();
    }

    private void startAffectingInDiscard(DefaultGame game, LotroPhysicalCard card) {
        if (isCardAffectingGame(card))
            ((PhysicalCardImpl) card).startAffectingGameInDiscard(game);
    }

    private void stopAffectingInDiscard(LotroPhysicalCard card) {
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

    public void setNextTribbleInSequence(int num) {
        _nextTribbleInSequence = num;
        for (GameStateListener listener : getAllGameStateListeners()) {
            DecimalFormat df = new DecimalFormat("#,###");
            listener.setTribbleSequence(df.format(num));
        }
    }

    public void setLastTribblePlayed(int num) {
        _lastTribblePlayed = num;
    }

    public int getLastTribblePlayed() { return _lastTribblePlayed; }

    public int getNextTribbleInSequence() { return _nextTribbleInSequence; }

    public void breakChain() {
        _chainBroken = true;
        sendMessage("The chain has been broken.");
        for (GameStateListener listener : getAllGameStateListeners()) {
            DecimalFormat df = new DecimalFormat("#,###");
            listener.setTribbleSequence("1 or " + df.format(_nextTribbleInSequence));
        }
    }

    public boolean isChainBroken() {
        return _chainBroken;
    }

    @Override
    public void playEffectReturningResult(LotroPhysicalCard cardPlayed) {
        setLastTribblePlayed(cardPlayed.getBlueprint().getTribbleValue());
        if (_lastTribblePlayed == 100000) {
            setNextTribbleInSequence(1);
        } else {
            setNextTribbleInSequence(_lastTribblePlayed * 10);
        }
        _chainBroken = false;
    }

    @Override
    public void playerPassEffect() {
        this.breakChain();
    }

    public void playerWentOut(String player) {
        // TODO
    }

    public int getRoundNum() {
        return _currentRound;
    }
    public boolean isLastRound() {
        return (_currentRound == 5);
    }

    public int getPlayerScore(String playerId) {
        return _players.get(playerId).getScore();
    }

    public void advanceRound() {
        // Each new round begins with a new "chain" (starting with a card worth 1 Tribble) and play proceeds clockwise.
        _chainBroken = false;
        setNextTribbleInSequence(1);
        _playerOrder.setReversed(false);

        // TODO: Handle "decked" players

        // Increment round number
        _currentRound++;
        sendMessage("Beginning Round " + _currentRound);
    }

}