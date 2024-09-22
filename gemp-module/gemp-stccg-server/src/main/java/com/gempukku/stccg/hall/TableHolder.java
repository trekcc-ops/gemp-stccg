package com.gempukku.stccg.hall;

import com.gempukku.stccg.cards.CardDeck;
import com.gempukku.stccg.db.IgnoreDAO;
import com.gempukku.stccg.db.vo.League;
import com.gempukku.stccg.game.CardGameMediator;
import com.gempukku.stccg.game.GameParticipant;
import com.gempukku.stccg.db.User;
import com.gempukku.stccg.league.LeagueSeriesData;
import com.gempukku.stccg.league.LeagueService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class TableHolder {
    private static final Logger LOGGER = LogManager.getLogger(TableHolder.class);
    private final LeagueService leagueService;
    private final IgnoreDAO ignoreDAO;

    private final Map<String, GameTable> awaitingTables = new LinkedHashMap<>();
    private final Map<String, GameTable> runningTables = new LinkedHashMap<>();

    private int _nextTableId = 1;

    public TableHolder(LeagueService leagueService, IgnoreDAO ignoreDAO) {
        this.leagueService = leagueService;
        this.ignoreDAO = ignoreDAO;
    }

    public int getTableCount() {
        return runningTables.size();
    }

    public void cancelWaitingTables() {
        awaitingTables.clear();
    }

    public GameTable createTable(User player, GameSettings gameSettings, CardDeck deck) throws HallException {
        LOGGER.debug("TableHolder - createTable function called");
        String tableId = String.valueOf(_nextTableId++);

        final League league = gameSettings.getLeague();
        if (league != null) {
            verifyNotPlayingLeagueGame(player, league);

            if (!leagueService.isPlayerInLeague(league, player))
                throw new HallException("You're not in that league");

            if (!leagueService.canPlayRankedGame(league, gameSettings.getSeriesData(), player.getName()))
                throw new HallException("You have already played max games in league");
        }

        GameTable table = new GameTable(gameSettings);

        boolean tableFull = table.addPlayer(new GameParticipant(player.getName(), deck));
        if (tableFull) {
            runningTables.put(tableId, table);
            return table;
        }

        awaitingTables.put(tableId, table);
        return null;
    }

    public GameTable joinTable(String tableId, User player, CardDeck deck) throws HallException {
        final GameTable awaitingTable = awaitingTables.get(tableId);

        if (awaitingTable == null || awaitingTable.wasGameStarted())
            throw new HallException("Table is already taken or was removed");

        if (awaitingTable.hasPlayer(player.getName()))
            throw new HallException("You can't play against yourself");

        final League league = awaitingTable.getGameSettings().getLeague();
        if (league != null) {
            verifyNotPlayingLeagueGame(player, league);

            if (!leagueService.isPlayerInLeague(league, player))
                throw new HallException("You're not in that league");

            LeagueSeriesData seriesData = awaitingTable.getGameSettings().getSeriesData();
            if (!leagueService.canPlayRankedGame(league, seriesData, player.getName()))
                throw new HallException("You have already played max games in league");
            if (!awaitingTable.getPlayerNames().isEmpty() && !leagueService.canPlayRankedGameAgainst(league, seriesData, awaitingTable.getPlayerNames().iterator().next(), player.getName()))
                throw new HallException("You have already played ranked league game against this player in that series");
        }

        final boolean tableFull = awaitingTable.addPlayer(new GameParticipant(player.getName(), deck));
        if (tableFull) {
            awaitingTables.remove(tableId);
            runningTables.put(tableId, awaitingTable);

            // Leave all other tables this player is waiting on
            for (GameParticipant awaitingTablePlayer : awaitingTable.getPlayers())
                leaveAwaitingTablesForPlayer(awaitingTablePlayer.getPlayerId());

            return awaitingTable;
        }
        return null;
    }

    public GameTable setupTournamentTable(GameSettings gameSettings, GameParticipant[] participants) {
        String tableId = String.valueOf(_nextTableId++);

        GameTable table = new GameTable(gameSettings);
        for (GameParticipant participant : participants) {
            table.addPlayer(participant);
        }
        runningTables.put(tableId, table);

        return table;
    }

    public GameSettings getGameSettings(String tableId) throws HallException {
        final GameTable gameTable = awaitingTables.get(tableId);
        if (gameTable != null)
            return gameTable.getGameSettings();
        GameTable runningTable = runningTables.get(tableId);
        if (runningTable != null)
            return runningTable.getGameSettings();
        throw new HallException("Table was already removed");
    }

    public boolean leaveAwaitingTable(User player, String tableId) {
        GameTable table = awaitingTables.get(tableId);
        if (table != null && table.hasPlayer(player.getName())) {
            boolean empty = table.removePlayer(player.getName());
            if (empty)
                awaitingTables.remove(tableId);
            return true;
        }
        return false;
    }

    public boolean leaveAwaitingTablesForPlayer(User player) {
        return leaveAwaitingTablesForPlayer(player.getName());
    }

    private boolean leaveAwaitingTablesForPlayer(String playerId) {
        boolean result = false;
        final Iterator<Map.Entry<String, GameTable>> iterator = awaitingTables.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<String, GameTable> table = iterator.next();
            if (table.getValue().hasPlayer(playerId)) {
                boolean empty = table.getValue().removePlayer(playerId);
                if (empty)
                    iterator.remove();
                result = true;
            }
        }
        return result;
    }

    private void verifyNotPlayingLeagueGame(User player, League league) throws HallException {
        for (GameTable awaitingTable : awaitingTables.values()) {
            if (league.equals(awaitingTable.getGameSettings().getLeague())
                    && awaitingTable.hasPlayer(player.getName())) {
                throw new HallException("You can't play in multiple league games at the same time");
            }
        }

        for (GameTable runningTable : runningTables.values()) {
            if (league.equals(runningTable.getGameSettings().getLeague())) {
                CardGameMediator game = runningTable.getMediator();
                if (game != null && !game.isFinished() && game.getPlayersPlaying().contains(player.getName()))
                    throw new HallException("You can't play in multiple league games at the same time");
            }
        }
    }

    public void processTables(boolean isAdmin, User player, HallInfoVisitor visitor) {
        // First waiting
        for (Map.Entry<String, GameTable> tableInformation : awaitingTables.entrySet()) {
            final GameTable table = tableInformation.getValue();

            List<String> players;
            if (table.getGameSettings().getLeague() != null)
                players = Collections.emptyList();
            else
                players = table.getPlayerNames();

            if (isAdmin || isNoIgnores(players, player.getName()))
                visitor.visitTable(tableInformation.getKey(), null, false, HallInfoVisitor.TableStatus.WAITING, "Waiting", table.getGameSettings().getGameFormat().getGameType(), table.getGameSettings().getGameFormat().getName(), getTournamentName(table), table.getGameSettings().getUserDescription(), players, table.getPlayerNames().contains(player.getName()), table.getGameSettings().isPrivateGame(), table.getGameSettings().isUserInviteOnly(), null);
        }

        // Then non-finished
        Map<String, GameTable> finishedTables = new HashMap<>();

        for (Map.Entry<String, GameTable> runningGame : runningTables.entrySet()) {
            final GameTable runningTable = runningGame.getValue();
            CardGameMediator cardGameMediator = runningTable.getMediator();
            if (cardGameMediator != null) {
                if (isAdmin || (cardGameMediator.isVisibleToUser(player.getName()) &&
                        isNoIgnores(cardGameMediator.getPlayersPlaying(), player.getName()))) {
                    if (cardGameMediator.isFinished())
                        finishedTables.put(runningGame.getKey(), runningTable);
                    else
                        visitor.visitTable(runningGame.getKey(), cardGameMediator.getGameId(),
                                isAdmin || cardGameMediator.isAllowSpectators(),
                                HallInfoVisitor.TableStatus.PLAYING, cardGameMediator.getGameStatus(),
                                runningTable.getGameSettings().getGameFormat().getGameType(),
                                runningTable.getGameSettings().getGameFormat().getName(),
                                getTournamentName(runningTable), runningTable.getGameSettings().getUserDescription(),
                                cardGameMediator.getPlayersPlaying(),
                                cardGameMediator.getPlayersPlaying().contains(player.getName()),
                                runningTable.getGameSettings().isPrivateGame(),
                                runningTable.getGameSettings().isUserInviteOnly(),
                                cardGameMediator.getWinner()
                        );

                    if (!cardGameMediator.isFinished() && cardGameMediator.getPlayersPlaying().contains(player.getName()))
                        visitor.runningPlayerGame(cardGameMediator.getGameId());
                }
            }
        }

        // Then rest
        for (Map.Entry<String, GameTable> nonPlayingGame : finishedTables.entrySet()) {
            final GameTable runningTable = nonPlayingGame.getValue();
            CardGameMediator cardGameMediator = runningTable.getMediator();
            if (cardGameMediator != null) {
                if (isAdmin || isNoIgnores(cardGameMediator.getPlayersPlaying(), player.getName()))
                    visitor.visitTable(nonPlayingGame.getKey(), cardGameMediator.getGameId(), false,
                            HallInfoVisitor.TableStatus.FINISHED, cardGameMediator.getGameStatus(),
                            runningTable.getGameSettings().getGameFormat().getGameType(),
                            runningTable.getGameSettings().getGameFormat().getName(), getTournamentName(runningTable),
                            runningTable.getGameSettings().getUserDescription(), cardGameMediator.getPlayersPlaying(),
                            cardGameMediator.getPlayersPlaying().contains(player.getName()),
                            runningTable.getGameSettings().isPrivateGame(),
                            runningTable.getGameSettings().isUserInviteOnly(), cardGameMediator.getWinner()
                    );
            }
        }
    }

    public void removeFinishedGames() {
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

    private String getTournamentName(GameTable table) {
        final League league = table.getGameSettings().getLeague();
        if (league != null)
            return league.getName() + " - " + table.getGameSettings().getSeriesData().getName();
        else
            return "Casual - " + table.getGameSettings().getTimeSettings().name();
    }
}
