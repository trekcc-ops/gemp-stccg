package com.gempukku.stccg.game;

import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.async.LoggingProxy;
import com.gempukku.stccg.database.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameHistoryService {
    private final GameHistoryDAO _gameHistoryDAO;
    private final Map<String, Integer> _playerGameCount = new ConcurrentHashMap<>();

    public GameHistoryService(DbAccess dbAccess) {
        _gameHistoryDAO =
                LoggingProxy.createLoggingProxy(GameHistoryDAO.class, new DbGameHistoryDAO(dbAccess));
    }

    public final int addGameHistory(DBData.GameHistory gh) {
        int id = _gameHistoryDAO.addGameHistory(gh.winner, gh.winnerId, gh.loser, gh.loserId, gh.win_reason,
                gh.lose_reason, gh.win_recording_id, gh.lose_recording_id, gh.format_name, gh.tournament,
                gh.winner_deck_name, gh.loser_deck_name, gh.start_date, gh.end_date, gh.replay_version);
        Integer winnerCount = _playerGameCount.get(gh.winner);
        Integer loserCount = _playerGameCount.get(gh.loser);
        if (winnerCount != null)
            _playerGameCount.put(gh.winner, winnerCount + 1);
        if (loserCount != null)
            _playerGameCount.put(gh.loser, loserCount + 1);
        return id;
    }

    public final boolean doesReplayIDExist(String id) {
        return _gameHistoryDAO.doesReplayIDExist(id);
    }

    public final DBData.GameHistory getGameHistory(String recordID) {
        return _gameHistoryDAO.getGameHistory(recordID);
    }

    public final List<DBData.GameHistory> getGameHistoryForPlayer(User user, int start, int count)
            throws HttpProcessingException {
        return _gameHistoryDAO.getGameHistoryForPlayer(user, start, count);
    }

    public final int getGameHistoryForPlayerCount(User player) {
        Integer result = _playerGameCount.get(player.getName());
        if (result != null)
            return result;
        int count = _gameHistoryDAO.getGameHistoryForPlayerCount(player);
        _playerGameCount.put(player.getName(), count);
        return count;
    }

    public final List<DBData.GameHistory> getGameHistoryForFormat(String format, int count) {
        return _gameHistoryDAO.getGameHistoryForFormat(format, count);
    }

    public final int getActivePlayersCount(ZonedDateTime from, ZonedDateTime duration) {
        return _gameHistoryDAO.getActivePlayersCount(from, duration);
    }

    public final int getGamesPlayedCount(ZonedDateTime from, ZonedDateTime duration) {
        return _gameHistoryDAO.getGamesPlayedCount(from, duration);
    }

    public final List<PlayerStatistic> getCasualPlayerStatistics(User player) {
        return _gameHistoryDAO.getCasualPlayerStatistics(player);
    }

    public final List<PlayerStatistic> getCompetitivePlayerStatistics(User player) {
        return _gameHistoryDAO.getCompetitivePlayerStatistics(player);
    }

    public Map<String, Object> serializeForServerStats(ZonedDateTime from, ZonedDateTime to) {
        Map<String, Object> result = new HashMap<>();
        result.put("ActivePlayers", getActivePlayersCount(from, to));
        result.put("GamesCount", getGamesPlayedCount(from, to));
        result.put("StartDate", from.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        result.put("EndDate", to.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        result.put("Stats", _gameHistoryDAO.GetAllGameFormatData(from, to));
        return result;
    }
}