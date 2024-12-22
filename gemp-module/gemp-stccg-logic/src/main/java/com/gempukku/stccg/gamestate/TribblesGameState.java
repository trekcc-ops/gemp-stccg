package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.TribblesPhysicalCard;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.filterable.SubDeck;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.TribblesGame;

import java.text.DecimalFormat;
import java.util.*;

public final class TribblesGameState extends GameState {
    private final Map<String, List<PhysicalCard>> _playPiles = new HashMap<>();
    private int _nextTribbleInSequence;
    private int _lastTribblePlayed;
    private boolean _chainBroken;
    private int _currentRound;
    private boolean _currentRoundIsOver;
    private final TribblesGame _game;

    public TribblesGameState(Iterable<String> playerIds, TribblesGame game) {
        super(game, playerIds);
        _currentRound = 0;
        _chainBroken = false;
        _game = game;
        setNextTribbleInSequence(1);
        for (String player : playerIds)
            _playPiles.put(player, new LinkedList<>());
    }

    @Override
    public TribblesGame getGame() { return _game; }

    @Override
    public List<PhysicalCard> getZoneCards(String playerId, Zone zone) {
        if (zone == Zone.DRAW_DECK || zone == Zone.HAND || zone == Zone.REMOVED || zone == Zone.DISCARD ||
                zone == Zone.VOID || zone == Zone.VOID_FROM_HAND)
            return _cardGroups.get(zone).get(playerId);
        else if (zone == Zone.PLAY_PILE)
            return _playPiles.get(playerId);
        else // This should never be accessed
            return _inPlay;
    }

    public void createPhysicalCards(CardBlueprintLibrary library, Map<String, CardDeck> decks) {
        for (Player player : getPlayers()) {
            String playerId = player.getPlayerId();
            for (Map.Entry<SubDeck,List<String>> entry : decks.get(playerId).getSubDecks().entrySet()) {
                List<PhysicalCard> subDeck = new LinkedList<>();
                for (String blueprintId : entry.getValue()) {
                    try {
                        CardBlueprint blueprint = library.getCardBlueprint(blueprintId);
                        PhysicalCard card = new TribblesPhysicalCard(_game, _nextCardId, player, blueprint);
                        subDeck.add(card);
                        _nextCardId++;
                    } catch (CardNotFoundException e) {
                        _game.sendErrorMessage(e);
                    }
                }
                if (Objects.equals(entry.getKey().name(), "DRAW_DECK")) {
                    _cardGroups.get(Zone.DRAW_DECK).put(playerId, subDeck);
                    subDeck.forEach(card -> card.setZone(Zone.DRAW_DECK));
                }
            }
            _playPiles.put(playerId, new LinkedList<>());
        }
    }


    public void shufflePlayPileIntoDeck(String playerId) {
        List<PhysicalCard> playPile = new LinkedList<>(getPlayPile(playerId));
        removeCardsFromZone(playerId, playPile);
        for (PhysicalCard card : playPile) {
            addCardToZone(card, Zone.DRAW_DECK);
        }
        shuffleDeck(playerId);
    }

    public List<PhysicalCard> getPlayPile(String playerId) {
        return Collections.unmodifiableList(_playPiles.get(playerId));
    }

    public void setPlayerDecked(String playerId, boolean bool) {
        Player player = getPlayer(playerId);
        player.setDecked(bool);
        for (GameStateListener listener : getAllGameStateListeners())
            listener.setPlayerDecked(player);
    }

    public boolean getPlayerDecked(String playerId) {
        return getPlayer(playerId).getDecked();
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

    public void playerWentOut() {
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
        _currentRoundIsOver = false;
        sendMessage("Beginning Round " + _currentRound);
    }

    public void endRound() {
        _currentRoundIsOver = true;
    }

    public boolean isCurrentRoundOver() { return _currentRoundIsOver; }
}