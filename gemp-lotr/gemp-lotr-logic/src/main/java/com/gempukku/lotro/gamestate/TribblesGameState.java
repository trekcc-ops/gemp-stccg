package com.gempukku.lotro.gamestate;

import com.gempukku.lotro.cards.CardBlueprintLibrary;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.cards.PhysicalCardImpl;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.*;
import com.gempukku.lotro.modifiers.ModifierFlag;
import org.apache.log4j.Logger;

import java.text.DecimalFormat;
import java.util.*;

public class TribblesGameState extends GameState {
    private static final Logger _log = Logger.getLogger(TribblesGameState.class);
    private PlayerOrder _playerOrder;
    private final Map<String, List<PhysicalCardImpl>> _playPiles = new HashMap<>();
    private final Map<String, List<PhysicalCardImpl>> _adventureDecks = new HashMap<>();
    private final Map<String, List<PhysicalCardImpl>> _decks = new HashMap<>();
    private final Map<String, List<PhysicalCardImpl>> _hands = new HashMap<>();
    private final Map<String, List<PhysicalCardImpl>> _discards = new HashMap<>();
    private final Map<String, List<PhysicalCardImpl>> _stacked = new HashMap<>();
    private final Map<String, List<PhysicalCardImpl>> _removed = new HashMap<>();
    private final List<PhysicalCardImpl> _inPlay = new LinkedList<>();
    private int _nextTribbleInSequence;
    private int _lastTribblePlayed;
    private boolean _chainBroken;
    private int _currentRound;

    public void init(PlayerOrder playerOrder, String firstPlayer, Map<String, List<String>> cards,
                     CardBlueprintLibrary library, GameFormat format) {
        _playerOrder = playerOrder;
        _currentRound = 0;
        _chainBroken = false;
        setNextTribbleInSequence(1);
        setCurrentPlayerId(firstPlayer);

        for (String player : playerOrder.getAllPlayers()) {
            _players.put(player, new Player(player));
        }

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

    public void loseControlOfCard(LotroPhysicalCard card, Zone zone) {
        ((PhysicalCardImpl) card).setCardController(null);
        ((PhysicalCardImpl) card).setZone(zone);
        for (GameStateListener listener : getAllGameStateListeners())
            listener.cardMoved(card);
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
            if (!zoneCards.contains(card)) {
                _log.error("Card was not found in the expected zone");
            }
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

        if (zone.isInPlay())
            if (card.getBlueprint().getCardType() != CardType.SITE || (getCurrentPhase() != Phase.PLAY_STARTING_FELLOWSHIP))
                startAffecting(game, card);

        if (zone == Zone.STACKED)
            startAffectingStacked(game, card);
        else if (zone == Zone.DISCARD)
            startAffectingInDiscard(game, card);
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

    public List<LotroPhysicalCard> getPlayPile(String playerId) {
        return Collections.unmodifiableList(_playPiles.get(playerId));
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