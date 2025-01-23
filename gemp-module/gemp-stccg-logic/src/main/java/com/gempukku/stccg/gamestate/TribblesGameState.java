package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.TribblesPhysicalCard;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.filterable.SubDeck;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.*;

import java.text.DecimalFormat;
import java.util.*;

public final class TribblesGameState extends GameState {
    private final Map<String, List<PhysicalCard>> _playPiles = new HashMap<>();
    private int _nextTribbleInSequence;
    private int _lastTribblePlayed;
    private boolean _chainBroken;
    private int _currentRound;
    private boolean _currentRoundIsOver;

    public TribblesGameState(Iterable<String> playerIds, TribblesGame game) {
        super(game, playerIds);
        _currentRound = 0;
        _chainBroken = false;
        setNextTribbleInSequence(game, 1);
        for (String player : playerIds)
            _playPiles.put(player, new LinkedList<>());
    }

    @Override
    public List<PhysicalCard> getZoneCards(Player player, Zone zone) {
        if (zone == Zone.DRAW_DECK || zone == Zone.HAND || zone == Zone.REMOVED || zone == Zone.DISCARD ||
                zone == Zone.VOID)
            return player.getCardGroupCards(zone);
        else if (zone == Zone.PLAY_PILE)
            return _playPiles.get(player.getPlayerId());
        else // This should never be accessed
            return _inPlay;
    }


    public void createPhysicalCards(TribblesGame game, CardBlueprintLibrary library, Map<String, CardDeck> decks) {
        try {
            for (Player player : getPlayers()) {
                String playerId = player.getPlayerId();
                for (Map.Entry<SubDeck, List<String>> entry : decks.get(playerId).getSubDecks().entrySet()) {
                    List<PhysicalCard> subDeck = new LinkedList<>();
                    for (String blueprintId : entry.getValue()) {
                        try {
                            CardBlueprint blueprint = library.getCardBlueprint(blueprintId);
                            PhysicalCard card = new TribblesPhysicalCard(game, _nextCardId, player, blueprint);
                            subDeck.add(card);
                            _nextCardId++;
                        } catch (CardNotFoundException e) {
                            game.sendErrorMessage(e);
                        }
                    }
                    if (Objects.equals(entry.getKey().name(), "DRAW_DECK")) {
                        player.setCardGroup(Zone.DRAW_DECK, subDeck);
                        subDeck.forEach(card -> card.setZone(Zone.DRAW_DECK));
                    }
                }
                _playPiles.put(playerId, new LinkedList<>());
            }
        } catch(InvalidGameLogicException exp) {
            game.sendErrorMessage(exp);
        }
    }


    public void shufflePlayPileIntoDeck(DefaultGame game, Player player) {
        String playerId = player.getPlayerId();
        List<PhysicalCard> playPile = new LinkedList<>(getPlayPile(playerId));
        removeCardsFromZone(game, playerId, playPile);
        for (PhysicalCard card : playPile) {
            addCardToZone(card, Zone.DRAW_DECK);
        }
        player.shuffleDrawDeck(game);
    }

    public List<PhysicalCard> getPlayPile(String playerId) {
        return Collections.unmodifiableList(_playPiles.get(playerId));
    }

    public void setPlayerDecked(DefaultGame cardGame, Player player, boolean bool) {
        player.setDecked(bool);
        for (GameStateListener listener : cardGame.getAllGameStateListeners())
            listener.setPlayerDecked(cardGame, player);
    }

    public void setNextTribbleInSequence(DefaultGame cardGame, int num) {
        _nextTribbleInSequence = num;
        for (GameStateListener listener : cardGame.getAllGameStateListeners()) {
            DecimalFormat df = new DecimalFormat("#,###");
            listener.setTribbleSequence(df.format(num));
        }
    }


    public void setLastTribblePlayed(int num) {
        _lastTribblePlayed = num;
    }

    public int getLastTribblePlayed() { return _lastTribblePlayed; }

    public int getNextTribbleInSequence() { return _nextTribbleInSequence; }

    public void breakChain(TribblesGame cardGame) {
        _chainBroken = true;
        cardGame.sendMessage("The chain has been broken.");
        for (GameStateListener listener : cardGame.getAllGameStateListeners()) {
            DecimalFormat df = new DecimalFormat("#,###");
            listener.setTribbleSequence("1 or " + df.format(_nextTribbleInSequence));
        }
    }


    public void setChainBroken(TribblesGame game, boolean chainBroken) {
        if (chainBroken) breakChain(game);
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

    public void checkVictoryConditions(DefaultGame cardGame) {
        // TODO - nothing to do here for now
    }

    public void advanceRound(TribblesGame cardGame) {
        // Each new round begins with a new "chain" (starting with a card worth 1 Tribble) and play proceeds clockwise.
        _chainBroken = false;
        setNextTribbleInSequence(cardGame, 1);
        _playerOrder.setReversed(false);

        // TODO: Handle "decked" players

        // Increment round number
        _currentRound++;
        _currentRoundIsOver = false;
        cardGame.sendMessage("Beginning Round " + _currentRound);
    }

    public void endRound() {
        _currentRoundIsOver = true;
    }

    public boolean isCurrentRoundOver() { return _currentRoundIsOver; }
}