package com.gempukku.stccg.gamestate;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.blueprints.CardBlueprint;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.TribblesPhysicalCard;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.common.GameTimer;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.SubDeck;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerClock;

import java.util.*;

public final class TribblesGameState extends GameState {
    private final Map<String, List<PhysicalCard>> _playPiles = new HashMap<>();
    private int _nextTribbleInSequence;
    private int _lastTribblePlayed;
    private boolean _chainBroken;
    private int _currentRound;
    private boolean _currentRoundIsOver;

    public TribblesGameState(Iterable<String> playerIds, TribblesGame game, GameTimer gameTimer) {
        super(game, playerIds, gameTimer);
        _currentRound = 0;
        _chainBroken = false;
        setNextTribbleInSequence(1);
        for (String player : playerIds)
            _playPiles.put(player, new LinkedList<>());
    }

    public TribblesGameState(Iterable<String> playerIds, TribblesGame game, Map<String, PlayerClock> clocks) {
        super(game, playerIds, clocks);
        _currentRound = 0;
        _chainBroken = false;
        setNextTribbleInSequence(1);
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
                            PhysicalCard card = new TribblesPhysicalCard(_nextCardId, player, blueprint);
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


    public List<PhysicalCard> getPlayPile(String playerId) {
        return Collections.unmodifiableList(_playPiles.get(playerId));
    }

    public void setPlayerDecked(Player player, boolean bool) {
        player.setDecked(bool);
    }

    public void setNextTribbleInSequence(int num) {
        _nextTribbleInSequence = num;
    }


    public void setLastTribblePlayed(int num) {
        _lastTribblePlayed = num;
    }

    public int getLastTribblePlayed() { return _lastTribblePlayed; }

    public int getNextTribbleInSequence() { return _nextTribbleInSequence; }

    public void breakChain() {
        _chainBroken = true;
    }


    public void setChainBroken(boolean chainBroken) {
        if (chainBroken) breakChain();
        else _chainBroken = false;
    }


    public boolean isChainBroken() {
        return _chainBroken;
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

    @Override
    public List<Phase> getPhasesInOrder() {
        return List.of(getCurrentPhase());
    }

    @Override
    public boolean cardsArePresentWithEachOther(PhysicalCard... cards) {
        return false;
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
    }

    public void endRound() {
        _currentRoundIsOver = true;
    }

    public boolean isCurrentRoundOver() { return _currentRoundIsOver; }
}