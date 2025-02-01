package com.gempukku.stccg.player;

import com.fasterxml.jackson.annotation.*;
import com.gempukku.stccg.game.ActionOrder;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@JsonIncludeProperties({ "turnOrder", "firstPlayer", "currentPlayer", "isReversed" })
@JsonPropertyOrder({ "turnOrder", "firstPlayer", "currentPlayer", "isReversed" })
public class PlayerOrder {
    private boolean _isReversed;
    private final List<String> _turnOrder = new LinkedList<>();
    @JsonProperty("firstPlayer")
    private final String _firstPlayer;
    @JsonProperty("currentPlayer")
    private String _currentPlayerId;
    @ConstructorProperties({"isReversed", "currentPlayer", "turnOrder"})
    public PlayerOrder(boolean isReversed, String currentPlayer, List<String> turnOrder) {
        _isReversed = isReversed;
        _firstPlayer = turnOrder.getFirst();
        _currentPlayerId = currentPlayer;
        _turnOrder.addAll(turnOrder);
    }

    public PlayerOrder(List<String> turnOrder) {
        this(false, turnOrder.getFirst(), turnOrder);
    }

    public String getFirstPlayer() {
        return _firstPlayer;
    }
    public String getCurrentPlayer() { return _currentPlayerId; }
    public void setCurrentPlayer(String player) { _currentPlayerId = player; }

    @JsonProperty("turnOrder")
    public List<String> getAllPlayers() {
        return Collections.unmodifiableList(_turnOrder);
    }

    @JsonIgnore
    public ActionOrder getCounterClockwisePlayOrder(String startingPlayerId, boolean looped) {
        int currentPlayerIndex = _turnOrder.indexOf(startingPlayerId);
        List<String> playOrder = new ArrayList<>();
        int nextIndex = currentPlayerIndex;
        do {
            playOrder.add(_turnOrder.get(nextIndex));
            nextIndex--;
            if (nextIndex < 0)
                nextIndex = _turnOrder.size() - 1;
        } while (currentPlayerIndex != nextIndex);
        return new ActionOrder(playOrder, looped);
    }

    @JsonIgnore
    public ActionOrder getClockwisePlayOrder(String startingPlayerId, boolean looped) {
        int currentPlayerIndex = _turnOrder.indexOf(startingPlayerId);
        List<String> playOrder = new ArrayList<>();
        int nextIndex = currentPlayerIndex;
        do {
            playOrder.add(_turnOrder.get(nextIndex));
            nextIndex++;
            if (nextIndex == _turnOrder.size())
                nextIndex = 0;
        } while (currentPlayerIndex != nextIndex);
        return new ActionOrder(playOrder, looped);
    }

    @JsonIgnore
    public ActionOrder getClockwisePlayOrder(Player startingPlayer, boolean looped) {
        return getClockwisePlayOrder(startingPlayer.getPlayerId(), looped);
    }


    @JsonIgnore
    public ActionOrder getStandardPlayOrder(String startingPlayerId, boolean looped) {
        if (!_isReversed) {
            return getClockwisePlayOrder(startingPlayerId, looped);
        } else {
            return getCounterClockwisePlayOrder(startingPlayerId, looped);
        }
    }

    @JsonIgnore
    public int getPlayerCount() {
        return _turnOrder.size();
    }
    public void advancePlayer() {
        int currentPlayerIndex = _turnOrder.indexOf(_currentPlayerId);
        if (currentPlayerIndex == _turnOrder.size() - 1) {
            currentPlayerIndex = 0;
        } else {
            currentPlayerIndex++;
        }
        _currentPlayerId = _turnOrder.get(currentPlayerIndex);
    }

    public void setReversed(boolean isReversed) { _isReversed = isReversed; }

    public void reversePlayerOrder() {
        _isReversed = !_isReversed;
    }
    @JsonProperty("isReversed")
    public boolean isReversed() { return _isReversed; }

}