package com.gempukku.lotro.gamestate;

import com.gempukku.lotro.cards.CardBlueprintLibrary;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.cards.PhysicalCardImpl;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.GameFormat;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.game.PlayerOrder;
import com.gempukku.lotro.game.TribblesGame;

import java.text.DecimalFormat;
import java.util.*;

public class TribblesGameState extends GameState {
    private final Map<String, List<PhysicalCardImpl>> _playPiles = new HashMap<>();
    private int _nextTribbleInSequence;
    private int _lastTribblePlayed;
    private boolean _chainBroken;
    private int _currentRound;

    public TribblesGameState(Map<String, List<String>> cards, CardBlueprintLibrary library, GameFormat format) {
        super(cards, library, format);
        for (Map.Entry<String, List<String>> stringListEntry : _cards.entrySet()) {
            _playPiles.put(stringListEntry.getKey(), new LinkedList<>());
        }
        _currentRound = 0;
        _chainBroken = false;
        setNextTribbleInSequence(1);
    }

    @Override
    public void init(PlayerOrder playerOrder, String firstPlayer) {
        _playerOrder = playerOrder;
        setCurrentPlayerId(firstPlayer);
        for (String player : playerOrder.getAllPlayers()) {
            _players.put(player, new Player(player));
        }
        for (GameStateListener listener : getAllGameStateListeners()) {
            listener.initializeBoard(playerOrder.getAllPlayers(), _format.discardPileIsPublic());
        }
    }

    @Override
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

    public void shufflePlayPileIntoDeck(TribblesGame game, String playerId) {
        List<LotroPhysicalCard> playPile = new LinkedList<>(getPlayPile(playerId));
        removeCardsFromZone(playerId, playPile);
        for (LotroPhysicalCard card : playPile) {
            addCardToZone(game, card, Zone.DECK);
        }
        shuffleDeck(playerId);
    }

    public List<LotroPhysicalCard> getPlayPile(String playerId) {
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