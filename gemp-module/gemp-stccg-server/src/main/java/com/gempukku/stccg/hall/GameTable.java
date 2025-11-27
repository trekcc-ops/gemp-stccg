package com.gempukku.stccg.hall;

import com.gempukku.stccg.database.User;
import com.gempukku.stccg.game.CardGameMediator;
import com.gempukku.stccg.game.GameParticipant;
import com.gempukku.stccg.league.League;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class GameTable {
    private static final Logger LOGGER = LogManager.getLogger(GameTable.class);

    private final GameSettings gameSettings;
    private final Map<String, GameParticipant> players = new HashMap<>();
    private final int _tableId;

    private CardGameMediator cardGameMediator;
    private final int capacity;
    private TableStatus _tableStatus;

    private enum TableStatus {
        WAITING, PLAYING, FINISHED
    }

    public GameTable(int tableId, GameSettings gameSettings) {
        this.gameSettings = gameSettings;
        this.capacity = 2; // manually change Tribbles player limit
        _tableId = tableId;
        _tableStatus = TableStatus.WAITING;
        LOGGER.debug("Capacity of game: {}", this.capacity);
    }

    public final void startGame(CardGameMediator cardGameMediator) {
        LOGGER.debug("GameTable - startGame function called;");
        this.cardGameMediator = cardGameMediator;
        cardGameMediator.startGame();
    }

    public final CardGameMediator getMediator() {
        return cardGameMediator;
    }

    public final boolean wasGameStarted() {
        return cardGameMediator != null;
    }

    public final void addPlayer(GameParticipant player) {
        players.put(player.getPlayerId(), player);
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

    Map<String, String> serializeForUser(User user) {

        String gameId = (_tableStatus == TableStatus.WAITING) ? null : cardGameMediator.getGameId();
        String statusDescription = (_tableStatus == TableStatus.WAITING) ?
                "Waiting" : cardGameMediator.getGame().getStatus();

        League league = gameSettings.getLeague();
        String tournamentName = (league != null) ?
                league.getName() + " - " + gameSettings.getSeriesData().getName() :
                "Casual - " + gameSettings.getTimeSettings().name();

        List<String> playerIds;
        boolean isPlaying;

        if (_tableStatus == TableStatus.WAITING) {
            playerIds = (gameSettings.getLeague() == null) ? getPlayerNames() : Collections.emptyList();
            isPlaying = getPlayerNames().contains(user.getName());
        } else {
            playerIds = cardGameMediator.getPlayersPlaying();
            isPlaying = playerIds.contains(user.getName());
        }

        Map<String, String> props = new HashMap<>();

        props.put("gameId", gameId);
        props.put("watchable", String.valueOf(isWatchableToUser(user)));
        props.put("status", String.valueOf(_tableStatus));
        props.put("statusDescription", statusDescription);
        props.put("gameType", gameSettings.getGameFormat().getGameType().name());
        props.put("format", gameSettings.getGameFormat().getName());
        props.put("userDescription", gameSettings.getUserDescription());
        props.put("isPrivate", String.valueOf(gameSettings.isPrivateGame()));
        props.put("isInviteOnly", String.valueOf(gameSettings.isUserInviteOnly()));
        props.put("tournament", tournamentName);
        props.put("players", StringUtils.join(playerIds, ","));
        props.put("playing", String.valueOf(isPlaying));
        if (_tableStatus != TableStatus.WAITING) {
            String winner = cardGameMediator.getWinner();
            if (winner != null)
                props.put("winner", winner);
        }

        return props;
    }

    private boolean isWatchableToUser(User user) {
        return switch(_tableStatus) {
            case WAITING, FINISHED -> false;
            case PLAYING -> user.isAdmin() || cardGameMediator.isAllowSpectators();
        };
    }

    public void setAsPlaying() { _tableStatus = TableStatus.PLAYING; }
    public void setAsFinished() { _tableStatus = TableStatus.FINISHED; }

    public int getTableId() { return _tableId; }

    public boolean isFull() {
        return players.size() == capacity;
    }

}