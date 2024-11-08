package com.gempukku.stccg.game;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@JsonSerialize(using = PlayerOrderSerializer.class)
public class PlayerOrder {
    private boolean _isReversed;
    private final List<String> _turnOrder;
    private final String _firstPlayer;
    private String _currentPlayer;
    public PlayerOrder(List<String> turnOrder) {
        _turnOrder = turnOrder;
        _isReversed = false;
        _firstPlayer = turnOrder.getFirst();
        _currentPlayer = turnOrder.getFirst();
    }

    public PlayerOrder(JsonNode node) {
        _isReversed = node.get("isReversed").booleanValue();
        _firstPlayer = node.get("firstPlayer").textValue();
        _currentPlayer = node.get("currentPlayer").textValue();
        _turnOrder = new LinkedList<>();
        for (JsonNode playerNode : node.get("turnOrder"))
            _turnOrder.add(playerNode.textValue());
    }

    public String getFirstPlayer() {
        return _firstPlayer;
    }
    public String getCurrentPlayer() { return _currentPlayer; }
    public void setCurrentPlayer(String player) { _currentPlayer = player; }

    public List<String> getAllPlayers() {
        return Collections.unmodifiableList(_turnOrder);
    }

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

    public ActionOrder getStandardPlayOrder(String startingPlayerId, boolean looped) {
        if (!_isReversed) {
            return getClockwisePlayOrder(startingPlayerId, looped);
        } else {
            return getCounterClockwisePlayOrder(startingPlayerId, looped);
        }
    }

    public int getPlayerCount() {
        return _turnOrder.size();
    }
    public void advancePlayer() {
        int currentPlayerIndex = _turnOrder.indexOf(_currentPlayer);
        if (currentPlayerIndex == _turnOrder.size() - 1) {
            currentPlayerIndex = 0;
        } else {
            currentPlayerIndex++;
        }
        _currentPlayer = _turnOrder.get(currentPlayerIndex);
    }

    public void setReversed(boolean isReversed) { _isReversed = isReversed; }

    public void reversePlayerOrder() {
        _isReversed = !_isReversed;
    }
    public boolean isReversed() { return _isReversed; }

}