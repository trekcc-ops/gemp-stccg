package com.gempukku.stccg.game;

import java.util.List;

public class ActionOrder {
    private final List<String> _playOrder;
    private final boolean _looped;
    private String _currentPlayer;
    private int _nextPlayerIndex;

    public ActionOrder(List<String> playOrder, boolean looped) {
        _playOrder = playOrder;
        _looped = looped;
    }

    public String getNextPlayer() {
        advancePlayer();
        return _currentPlayer;
    }

    public void advancePlayer() {
        if (_nextPlayerIndex >= getPlayerCount()) {
            _currentPlayer = null;
        } else {
            String nextPlayer = _playOrder.get(_nextPlayerIndex);
            _nextPlayerIndex++;
            if (_nextPlayerIndex >= getPlayerCount() && _looped)
                _nextPlayerIndex = 0;
            _currentPlayer = nextPlayer;
        }
    }

    public String getCurrentPlayerName() {
        return _currentPlayer;
    }

    public int getPlayerCount() {
        return _playOrder.size();
    }
}