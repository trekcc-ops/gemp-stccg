package com.gempukku.stccg.game;

import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;

import java.util.HashSet;
import java.util.Set;

public class Player {

    private final String _playerId;
    private boolean _decked;
    private int _score;
    private final Set<Affiliation> _playedAffiliations;
    private final DefaultGame _game;

    public Player(DefaultGame game, String playerId) {
        _playerId = playerId;
        _decked = false;
        _score = 0;
        _playedAffiliations = new HashSet<>();
        _game = game;
    }

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
        _score += points;
    }

    public int getScore() {
        return _score;
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

}
