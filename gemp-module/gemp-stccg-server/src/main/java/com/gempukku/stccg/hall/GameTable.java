package com.gempukku.stccg.hall;

import com.gempukku.stccg.database.User;
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
        this.capacity = 2; // manually change Tribbles player limit
        LOGGER.debug("Capacity of game: {}", this.capacity);
    }

    public final void startGame(CardGameMediator cardGameMediator) {
        LOGGER.debug("GameTable - startGame function called;");
        this.cardGameMediator = cardGameMediator;
    }

    public final CardGameMediator getMediator() {
        return cardGameMediator;
    }

    public final boolean wasGameStarted() {
        return cardGameMediator != null;
    }

    public final boolean addPlayer(GameParticipant player) {
        players.put(player.getPlayerId(), player);
        return players.size() == capacity;
    }

    public final boolean removePlayer(String playerId) {
        players.remove(playerId);
        return players.isEmpty();
    }

    public final boolean hasPlayer(String playerId) {
        return players.containsKey(playerId);
    }
    public final boolean hasPlayer(User user) { return hasPlayer(user.getName()); }

    public final List<String> getPlayerNames() {
        return new LinkedList<>(players.keySet());
    }

    public final Set<GameParticipant> getPlayers() {
        return Set.copyOf(players.values());
    }

    public final GameSettings getGameSettings() {
        return gameSettings;
    }
}