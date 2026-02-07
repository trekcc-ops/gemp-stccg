package com.gempukku.stccg.hall;

import com.gempukku.stccg.common.CardDeck;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.game.CardGameMediator;
import com.gempukku.stccg.game.GameParticipant;
import com.gempukku.stccg.game.GameServer;
import com.gempukku.stccg.league.League;
import com.gempukku.stccg.league.LeagueService;
import com.gempukku.stccg.service.AdminService;

import java.util.*;

public class TableHolder {
    private final LeagueService _leagueService;
    private final AdminService _adminService;
    private final Map<String, GameTable> awaitingTables = new LinkedHashMap<>();
    private final Map<String, GameTable> runningTables = new LinkedHashMap<>();

    public TableHolder(AdminService adminService, LeagueService leagueService) {
        _leagueService = leagueService;
        _adminService = adminService;
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

            if (!_leagueService.isPlayerInLeague(league, userName))
                throw new HallException("You're not in that league");

            if (!_leagueService.canPlayRankedGame(league, gameSettings.getSeries(), userName))
                throw new HallException("You have already played max games in league");
        }
    }

    public final GameTable createTable(GameSettings gameSettings, GameParticipant... participants) {
        GameTable table = new GameTable(gameSettings, participants);
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

    public final void joinTable(String tableId, User player, CardDeck deck, GameServer gameServer,
                                     HallServer hallServer, LeagueService leagueService) throws HallException {
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
        awaitingTable.createGameIfFull(gameServer, hallServer, leagueService);
        hallServer.hallChanged();
    }


    public final GameSettings getGameSettings(String tableId) throws HallException {
        GameTable gameTable = awaitingTables.get(tableId);
        if (gameTable == null) {
            gameTable = runningTables.get(tableId);
        }
        if (gameTable == null) {
            throw new HallException("Table was already removed");
        } else {
            return gameTable.getGameSettings();
        }
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

    final Set<String> getPlayedGames(User player) {
        Set<String> result = new HashSet<>();
        for (Map.Entry<String, GameTable> runningGame : runningTables.entrySet()) {
            final GameTable runningTable = runningGame.getValue();
            CardGameMediator cardGameMediator = runningTable.getMediator();
            if (cardGameMediator != null) {
                boolean isVisibleToUser = !runningTable.getGameSettings().isHiddenGame() ||
                        runningTable.hasPlayer(player);
                if (player.isAdmin() || (isVisibleToUser &&
                        isNoIgnores(runningTable.getPlayerNames(), player.getName()))) {
                    if (!runningTable.isGameFinished() && runningTable.hasPlayer(player)) {
                            result.add(cardGameMediator.getGameId());
                    }
                }
            }
        }
        return result;
    }

    final Map<String, GameTableView> getSerializedTables(User player) {
        Map<String, GameTableView> result = new HashMap<>();
        List<GameTable> finishedTables = new ArrayList<>();

        List<GameTable> tablesInOrder = new ArrayList<>(awaitingTables.values());
        for (GameTable table : runningTables.values()) {
            if (table.isGameFinished()) {
                finishedTables.add(table);
            } else {
                tablesInOrder.add(table);
            }
        }
        tablesInOrder.addAll(finishedTables);

        for (GameTable table : tablesInOrder) {
            boolean serializeTable = shouldTableBeSerializedForUser(table, player);
            if (serializeTable) {
                result.put(String.valueOf(table.getTableId()), new GameTableView(table, player));
            }
        }
        return result;
    }

    private boolean shouldTableBeSerializedForUser(GameTable table, User player) {
        if (player.isAdmin()) {
            return true;
        }
        List<String> players = (table.isForLeague() && table.getStatus() == GameTable.TableStatus.WAITING) ?
                Collections.emptyList() : table.getPlayerNames();
        if (!isNoIgnores(players, player.getName())) {
            return false;
        }
        if (table.getStatus() == GameTable.TableStatus.PLAYING) {
            return !table.getGameSettings().isHiddenGame() || table.hasPlayer(player);
        }
        return true;
    }

    public final void removeFinishedGames() {
        runningTables.values().removeIf(GameTable::shouldBeRemoved);
    }

    private boolean isNoIgnores(Collection<String> participants, String playerLooking) {
        // Do not ignore your own stuff
        if (participants.contains(playerLooking))
            return true;

        // This player ignores someone of the participants
        final Set<String> ignoredUsers = _adminService.getIgnoredUsers(playerLooking);
        if (!Collections.disjoint(ignoredUsers, participants))
            return false;

        // One of the participants ignores this player
        for (String player : participants) {
            final Set<String> ignored = _adminService.getIgnoredUsers(player);
            if (ignored.contains(playerLooking))
                return false;
        }

        return true;
    }

}