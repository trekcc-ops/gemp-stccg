package com.gempukku.stccg.game;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.gamestate.GameState;

import java.util.*;

public class Player implements Snapshotable<Player> {
    private final String _playerId;
    private boolean _decked;
    private final Collection<Affiliation> _playedAffiliations = EnumSet.noneOf(Affiliation.class);
    private final DefaultGame _game;

    public Player(DefaultGame game, String playerId) {
        _playerId = playerId;
        _decked = false;
        _game = game;
    }

    public DefaultGame getGame() { return _game; }

    public String getPlayerId() {
        return _playerId;
    }

    public boolean getDecked() {
        return _decked;
    }
    public void setDecked(boolean decked) {
        _decked = decked;
    }

    public void scorePoints(int points) {
        GameState gameState = _game.getGameState();
        gameState.addToPlayerScore(_playerId, points);
    }

    public boolean isPlayingAffiliation(Affiliation affiliation) {
        return _playedAffiliations.contains(affiliation);
    }

    public void addPlayedAffiliation(Affiliation affiliation) {
        _playedAffiliations.add(affiliation);
    }

    public boolean hasCardInZone(Zone zone, int count, Filterable... cardFilter) {
        if (zone == Zone.HAND)
            return Filters.filter(_game.getGameState().getHand(_playerId), _game, cardFilter).size() >= count;
        else if (zone == Zone.DISCARD)
            return Filters.filter(_game.getGameState().getDiscard(_playerId), _game, cardFilter).size() >= count;
        else
            return false;
    }

    public boolean canDiscardFromHand(int count, Filterable... cardFilter) {
        return hasCardInZone(Zone.HAND, count, cardFilter);
    }

    public boolean hasACopyOfCardInPlay(PhysicalCard card) {
        for (PhysicalCard cardInPlay : _game.getGameState().getAllCardsInPlay()) {
            if (cardInPlay.isCopyOf(card) && cardInPlay.getOwner() == this)
                return true;
        }
        return false;
    }

    public boolean canLookOrRevealCardsInHandOfPlayer(String targetPlayerId) {
        return _game.getModifiersQuerying().canLookOrRevealCardsInHand(targetPlayerId, _playerId);
    }

    @Override
    public Player generateSnapshot(SnapshotData snapshotData) {
        Player newPlayer = new Player(_game, _playerId);
        newPlayer._decked = _decked;
        newPlayer._playedAffiliations.clear();
        newPlayer._playedAffiliations.addAll(_playedAffiliations);
        return newPlayer;
    }

    public int getScore() {
        return _game.getGameState().getPlayerScore(_playerId);
    }
}