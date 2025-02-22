package com.gempukku.stccg.competitive;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.database.User;

public class PlayerStanding {

    @JsonProperty("playerName")
    private final String _playerName;

    @JsonProperty("points")
    private final int _points;

    @JsonProperty("gamesPlayed")
    private final int _gamesPlayed;
    private final int _playerWins;
    private final int _playerByes;

    @JsonProperty("opponentWin")
    private float _opponentWin;

    @JsonProperty("currentStanding")
    private int _standing;

    public PlayerStanding(String playerName, int points, int gamesPlayed, int playerWins, int playerByes) {
        _playerName = playerName;
        _points = points;
        _gamesPlayed = gamesPlayed;
        _playerWins = playerWins;
        _playerByes = playerByes;
    }

    public int getGamesPlayed() {
        return _gamesPlayed;
    }

    public float getOpponentWin() {
        return _opponentWin;
    }

    public String getPlayerName() {
        return _playerName;
    }

    public int getPoints() {
        return _points;
    }

    public int getStanding() {
        return _standing;
    }

    public int getPlayerWins() {
        return _playerWins;
    }

    public int getPlayerByes() {
        return _playerByes;
    }

    public void setOpponentWin(float opponentWin) {
        _opponentWin = opponentWin;
    }

    public void setStanding(int standing) {
        _standing = standing;
    }

}