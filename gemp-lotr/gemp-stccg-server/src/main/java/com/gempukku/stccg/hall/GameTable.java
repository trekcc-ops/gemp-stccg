package com.gempukku.stccg.hall;

import com.gempukku.stccg.game.CardGameMediator;
import com.gempukku.stccg.game.GameParticipant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class GameTable {
    private static final Logger LOGGER = LogManager.getLogger(GameTable.class);

    private final GameSettings gameSettings;
    private final Map<String, GameParticipant> players = new HashMap<>();

    private CardGameMediator cardGameMediator;
    private final int capacity;

    public GameTable(GameSettings gameSettings) {
        this.gameSettings = gameSettings;
        String formatName = gameSettings.getGameFormat().getName();
        this.capacity = 2; // manually change Tribbles player limit
        LOGGER.debug("Capacity of game: " + this.capacity);
    }

    public void startGame(CardGameMediator cardGameMediator) {
        LOGGER.debug("GameTable - startGame function called;");
        this.cardGameMediator = cardGameMediator;
    }

    public CardGameMediator getMediator() {
        return cardGameMediator;
    }

    public boolean wasGameStarted() {
        return cardGameMediator != null;
    }

    public boolean addPlayer(GameParticipant player) {
        players.put(player.getPlayerId(), player);
        return players.size() == capacity;
    }

    public boolean removePlayer(String playerId) {
        players.remove(playerId);
        return players.isEmpty();
    }

    public boolean hasPlayer(String playerId) {
        return players.containsKey(playerId);
    }

    public List<String> getPlayerNames() {
        return new LinkedList<>(players.keySet());
    }

    public Set<GameParticipant> getPlayers() {
        return Set.copyOf(players.values());
    }

    public GameSettings getGameSettings() {
        return gameSettings;
    }
}
