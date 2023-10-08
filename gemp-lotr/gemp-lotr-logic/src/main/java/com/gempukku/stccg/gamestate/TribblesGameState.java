package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardDeck;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.Zone;
import com.gempukku.stccg.formats.GameFormat;
import com.gempukku.stccg.game.TribblesGame;

import java.text.DecimalFormat;
import java.util.*;

public class TribblesGameState extends GameState {
    protected final Map<String, List<PhysicalCard>> _playPiles = new HashMap<>();
    private int _nextTribbleInSequence;
    private int _lastTribblePlayed;
    private boolean _chainBroken;
    private int _currentRound;

    public TribblesGameState(Set<String> players, Map<String, CardDeck> decks, CardBlueprintLibrary library, GameFormat format) {
        super(players, decks, library, format);
        for (String player : players) {
            _playPiles.put(player, new LinkedList<>());
        }
        _currentRound = 0;
        _chainBroken = false;
        setNextTribbleInSequence(1);
    }

    @Override
    public List<PhysicalCard> getZoneCards(String playerId, Zone zone) {
        if (zone == Zone.DRAW_DECK)
            return this._drawDecks.get(playerId);
        else if (zone == Zone.PLAY_PILE)
            return _playPiles.get(playerId);
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

    @Override
    public void createPhysicalCards() {
        int cardId = 1;
        for (String playerId : _players.keySet()) {
            for (Map.Entry<String,List<String>> entry : _decks.get(playerId).getSubDecks().entrySet()) {
                List<PhysicalCard> subDeck = new LinkedList<>();
                for (String blueprintId : entry.getValue()) {
                    try {
                        subDeck.add(new PhysicalCard(cardId, blueprintId, playerId, _library.getLotroCardBlueprint(blueprintId)));
                        cardId++;
                    } catch (CardNotFoundException e) {
                        throw new RuntimeException("Card blueprint not found");
                    }
                }
                if (Objects.equals(entry.getKey(), "DRAW_DECK")) {
                    this._drawDecks.put(playerId, subDeck);
                    subDeck.forEach(card -> card.setZone(Zone.DRAW_DECK));
                }
            }
            _playPiles.put(playerId, new LinkedList<>());
        }
    }

    public void shufflePlayPileIntoDeck(TribblesGame game, String playerId) {
        List<PhysicalCard> playPile = new LinkedList<>(getPlayPile(playerId));
        removeCardsFromZone(playerId, playPile);
        for (PhysicalCard card : playPile) {
            addCardToZone(game, card, Zone.DRAW_DECK);
        }
        shuffleDeck(playerId);
    }

    public List<PhysicalCard> getPlayPile(String playerId) {
        return Collections.unmodifiableList(_playPiles.get(playerId));
    }
    public void setPlayerDecked(String playerId, boolean bool) {
        _players.get(playerId).setDecked(bool);
        for (GameStateListener listener : getAllGameStateListeners())
            listener.setPlayerDecked(playerId, bool);
    }

    public boolean getPlayerDecked(String playerId) {
        return _players.get(playerId).getDecked();
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

    public void setChainBroken(boolean chainBroken) {
        if (chainBroken) breakChain();
        else _chainBroken = false;
    }

    public boolean isChainBroken() {
        return _chainBroken;
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