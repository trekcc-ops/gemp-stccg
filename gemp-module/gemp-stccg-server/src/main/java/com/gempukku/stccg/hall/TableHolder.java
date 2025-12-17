package com.gempukku.stccg.hall;

import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.database.IgnoreDAO;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.game.CardGameMediator;
import com.gempukku.stccg.game.GameParticipant;
import com.gempukku.stccg.league.League;
import com.gempukku.stccg.league.LeagueService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

class TableHolder {
    private static final Logger LOGGER = LogManager.getLogger(TableHolder.class);
    private final LeagueService leagueService;
    private final IgnoreDAO ignoreDAO;
    private final Map<String, GameTable> awaitingTables = new LinkedHashMap<>();
    private final Map<String, GameTable> runningTables = new LinkedHashMap<>();

    private int _nextTableId = 1;

    public TableHolder(ServerObjects objects) {
        this.leagueService = objects.getLeagueService();
        this.ignoreDAO = objects.getIgnoreDAO();
    }

    public final int getTableCount() {
        return runningTables.size();
    }

    public final void cancelWaitingTables() {
        awaitingTables.clear();
    }

    private List<GameTable> getActiveLeagueTablesForUser(String userName, League league) {
        List<GameTable> result = new ArrayList<>();
        for (GameTable awaitingTable : awaitingTables.values()) {
            if (awaitingTable.playerIsPlayingForLeague(userName, league)) {
                result.add(awaitingTable);
            }
        }

        for (GameTable runningTable : runningTables.values()) {
            if (runningTable.playerIsPlayingForLeague(userName, league)) {
                result.add(runningTable);
            }
        }
        return result;
    }

    public void validatePlayerForLeague(String userName, GameSettings gameSettings) throws HallException {
        final League league = gameSettings.getLeague();
        if (league != null) {
            List<GameTable> activeLeagueTables = getActiveLeagueTablesForUser(userName, league);
            if (!activeLeagueTables.isEmpty()) {
                throw new HallException("You can't play in multiple league games at the same time");
            }

            if (!leagueService.isPlayerInLeague(league, userName))
                throw new HallException("You're not in that league");

            if (!leagueService.canPlayRankedGame(league, gameSettings.getSeriesData(), userName))
                throw new HallException("You have already played max games in league");
        }
    }

    public final GameTable createTable(GameSettings gameSettings, GameParticipant... participants) {
        int tableId = _nextTableId;
        _nextTableId++;
        GameTable table = new GameTable(tableId, gameSettings, participants);
        awaitingTables.put(String.valueOf(table.getTableId()), table);
        runTableIfFull(table);
        return table;
    }

    private void runTableIfFull(GameTable table) {
        if (table.isFull()) {
            String tableId = String.valueOf(table.getTableId());
            awaitingTables.remove(tableId);
            runningTables.put(tableId, table);
            table.setAsPlaying();

            // Leave all other tables players are waiting on
            for (GameParticipant awaitingTablePlayer : table.getPlayers()) {
                leaveAwaitingTablesForPlayer(awaitingTablePlayer.getPlayerId());
            }
        }
    }

    public final void joinTable(String tableId, User player, CardDeck deck, ServerObjects serverObjects,
                                     HallServer hallServer) throws HallException {
        final GameTable awaitingTable = awaitingTables.get(tableId);
        String userName = player.getName();

        if (awaitingTable == null || awaitingTable.wasGameStarted())
            throw new HallException("Table is already taken or was removed");

        if (awaitingTable.hasPlayer(player))
            throw new HallException("You can't play against yourself");

        validatePlayerForLeague(userName, awaitingTable.getGameSettings());
        awaitingTable.validateOpponentForLeague(userName, leagueService);
        awaitingTable.addPlayer(new GameParticipant(userName, deck));
        runTableIfFull(awaitingTable);
        awaitingTable.createGameIfFull(serverObjects);
        hallServer.hallChanged();
    }


    public final GameSettings getGameSettings(String tableId) throws HallException {
        final GameTable gameTable = awaitingTables.get(tableId);
        if (gameTable != null)
            return gameTable.getGameSettings();
        GameTable runningTable = runningTables.get(tableId);
        if (runningTable != null)
            return runningTable.getGameSettings();
        throw new HallException("Table was already removed");
    }

    public final void leaveAwaitingTable(User player, String tableId) {
        GameTable table = awaitingTables.get(tableId);
        if (table != null) {
            table.removePlayer(player.getName());
            if (table.isEmpty())
                awaitingTables.remove(String.valueOf(table.getTableId()));
        }
    }

    public boolean leaveAwaitingTablesForPlayer(String playerId) {
        boolean result = false;
        final Iterator<Map.Entry<String, GameTable>> iterator = awaitingTables.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<String, GameTable> table = iterator.next();
            GameTable gameTable = table.getValue();
            if (gameTable.hasPlayer(playerId)) {
                gameTable.removePlayer(playerId);
                if (gameTable.isEmpty())
                    iterator.remove();
                result = true;
            }
        }
        return result;
    }

    final void processTables(User player, Set<String> playedGamesOnServer,
                             Map<String, GameTableView> tablesOnServer) {

        boolean isAdmin = player.isAdmin();

        // First waiting
        for (Map.Entry<String, GameTable> tableInformation : awaitingTables.entrySet()) {
            final GameTable table = tableInformation.getValue();
            String tableId = tableInformation.getKey();
            List<String> players = (table.isForLeague()) ? Collections.emptyList() : table.getPlayerNames();
            if (isAdmin || isNoIgnores(players, player.getName())) {
                tablesOnServer.put(tableId, new GameTableView(table, player));
            }
        }

        // Then non-finished
        Map<String, GameTable> finishedTables = new HashMap<>();

        for (Map.Entry<String, GameTable> runningGame : runningTables.entrySet()) {
            final GameTable runningTable = runningGame.getValue();
            CardGameMediator cardGameMediator = runningTable.getMediator();
            if (cardGameMediator != null) {
                boolean isVisibleToUser = !runningTable.getGameSettings().isHiddenGame() || runningTable.hasPlayer(player);
                if (isAdmin || (isVisibleToUser &&
                        isNoIgnores(runningTable.getPlayerNames(), player.getName()))) {
                    if (runningTable.isGameFinished()) {
                        runningTable.setAsFinished();
                        finishedTables.put(runningGame.getKey(), runningTable);
                    } else {
                        tablesOnServer.put(runningGame.getKey(), new GameTableView(runningTable, player));
                        if (runningTable.hasPlayer(player))
                            playedGamesOnServer.add(cardGameMediator.getGameId());
                    }
                }
            }
        }

        // Then rest
        for (Map.Entry<String, GameTable> nonPlayingGame : finishedTables.entrySet()) {
            final GameTable runningTable = nonPlayingGame.getValue();
            CardGameMediator cardGameMediator = runningTable.getMediator();
            if (cardGameMediator != null) {
                if (isAdmin || isNoIgnores(runningTable.getPlayerNames(), player.getName())) {
                    tablesOnServer.put(nonPlayingGame.getKey(), new GameTableView(runningTable, player));
                }
            }
        }
    }

    public final void removeFinishedGames() {
        final Iterator<Map.Entry<String, GameTable>> iterator = runningTables.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<String, GameTable> runningTable = iterator.next();
            CardGameMediator cardGameMediator = runningTable.getValue().getMediator();
            if (cardGameMediator.isDestroyed()) {
                iterator.remove();
            }
        }
    }

    private boolean isNoIgnores(Collection<String> participants, String playerLooking) {
        // Do not ignore your own stuff
        if (participants.contains(playerLooking))
            return true;

        // This player ignores someone of the participants
        final Set<String> ignoredUsers = ignoreDAO.getIgnoredUsers(playerLooking);
        if (!Collections.disjoint(ignoredUsers, participants))
            return false;

        // One of the participants ignores this player
        for (String player : participants) {
            final Set<String> ignored = ignoreDAO.getIgnoredUsers(player);
            if (ignored.contains(playerLooking))
                return false;
        }

        return true;
    }

}