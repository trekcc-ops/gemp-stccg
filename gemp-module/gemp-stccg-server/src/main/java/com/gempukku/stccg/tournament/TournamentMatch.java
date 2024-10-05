package com.gempukku.stccg.tournament;

import com.gempukku.stccg.competitive.CompetitiveMatchResult;

public class TournamentMatch implements CompetitiveMatchResult {
    private final String _playerOne;
    private final String _playerTwo;
    private final String _winner;

    public TournamentMatch(String playerOne, String playerTwo, String winner) {
        _playerOne = playerOne;
        _playerTwo = playerTwo;
        _winner = winner;
    }

    public String getPlayerOne() {
        return _playerOne;
    }

    public String getPlayerTwo() {
        return _playerTwo;
    }

    public boolean isFinished() {
        return _winner != null;
    }

    @Override
    public String getWinner() {
        return _winner;
    }

    @Override
    public String getLoser() {
        return _playerOne.equals(_winner) ? _playerTwo : _playerOne;
    }
}
