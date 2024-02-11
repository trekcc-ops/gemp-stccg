package com.gempukku.stccg.game;

import com.gempukku.stccg.common.filterable.Affiliation;

import java.util.HashSet;
import java.util.Set;

public class Player {

    private final String _playerId;
    private boolean _decked;
    private int _score;
    private final Set<Affiliation> _playedAffiliations;

    public Player(String playerId) {
        _playerId = playerId;
        _decked = false;
        _score = 0;
        _playedAffiliations = new HashSet<>();
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

}
