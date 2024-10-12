package com.gempukku.stccg.competitive;

public class PlayerStanding {
    private final String _playerName;
    private final int _points;
    private final int _gamesPlayed;
    private final int _playerWins;
    private final int _playerByes;
    private float _opponentWin;
    private int _standing;

    public PlayerStanding(String playerName, int points, int gamesPlayed, int playerWins, int playerByes) {
        _playerName = playerName;
        _points = points;
        _gamesPlayed = gamesPlayed;
        _playerWins = playerWins;
        _playerByes = playerByes;
    }

    public final int getGamesPlayed() {
        return _gamesPlayed;
    }

    public final float getOpponentWin() {
        return _opponentWin;
    }

    public final String getPlayerName() {
        return _playerName;
    }

    public final int getPoints() {
        return _points;
    }

    public final int getStanding() {
        return _standing;
    }

    public final int getPlayerWins() {
        return _playerWins;
    }

    public final int getPlayerByes() {
        return _playerByes;
    }

    public final void setOpponentWin(float opponentWin) {
        _opponentWin = opponentWin;
    }

    public final void setStanding(int standing) {
        _standing = standing;
    }
}