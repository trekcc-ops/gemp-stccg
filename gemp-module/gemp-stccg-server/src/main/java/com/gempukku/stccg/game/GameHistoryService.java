package com.gempukku.stccg.game;

import com.gempukku.stccg.database.DBData;
import com.gempukku.stccg.common.JSONData;
import com.gempukku.stccg.database.GameHistoryDAO;
import com.gempukku.stccg.database.PlayerStatistic;
import com.gempukku.stccg.database.User;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameHistoryService {
    private final GameHistoryDAO _gameHistoryDAO;
    private final Map<String, Integer> _playerGameCount = new ConcurrentHashMap<>();

    public GameHistoryService(GameHistoryDAO gameHistoryDAO) {
        _gameHistoryDAO = gameHistoryDAO;
    }

    public final int addGameHistory(DBData.GameHistory gh) {
        return addGameHistory(gh.winner, gh.winnerId, gh.loser, gh.loserId, gh.win_reason, gh.lose_reason,
                gh.win_recording_id, gh.lose_recording_id, gh.format_name, gh.tournament, gh.winner_deck_name,
                gh.loser_deck_name, gh.start_date, gh.end_date, gh.replay_version
        );
    }

    private final int addGameHistory(String winner, int winnerId, String loser, int loserId, String winReason,
                                     String loseReason, String winRecordingId, String loseRecordingId,
                                     String formatName, String tournament, String winnerDeckName, String loserDeckName,
                                     ZonedDateTime startDate, ZonedDateTime endDate, int version) {
        int id = _gameHistoryDAO.addGameHistory(winner, winnerId, loser, loserId, winReason, loseReason,
                winRecordingId, loseRecordingId, formatName, tournament, winnerDeckName, loserDeckName, startDate,
                endDate, version);
        Integer winnerCount = _playerGameCount.get(winner);
        Integer loserCount = _playerGameCount.get(loser);
        if (winnerCount != null)
            _playerGameCount.put(winner, winnerCount + 1);
        if (loserCount != null)
            _playerGameCount.put(loser, loserCount + 1);

        return id;
    }

    public final boolean doesReplayIDExist(String id) {
        return _gameHistoryDAO.doesReplayIDExist(id);
    }

    public final DBData.GameHistory getGameHistory(String recordID) {
        return _gameHistoryDAO.getGameHistory(recordID);
    }

    public final int getGameHistoryForPlayerCount(User player) {
        Integer result = _playerGameCount.get(player.getName());
        if (result != null)
            return result;
        int count = _gameHistoryDAO.getGameHistoryForPlayerCount(player);
        _playerGameCount.put(player.getName(), count);
        return count;
    }

    public final List<DBData.GameHistory> getGameHistoryForPlayer(User player, int start, int count) {
        return _gameHistoryDAO.getGameHistoryForPlayer(player, start, count);
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

    public final List<JSONData.FormatStats> getGameHistoryStatistics(ZonedDateTime from, ZonedDateTime to) {
        return _gameHistoryDAO.GetAllGameFormatData(from, to);
    }

    public final List<PlayerStatistic> getCasualPlayerStatistics(User player) {
        return _gameHistoryDAO.getCasualPlayerStatistics(player);
    }

    public final List<PlayerStatistic> getCompetitivePlayerStatistics(User player) {
        return _gameHistoryDAO.getCompetitivePlayerStatistics(player);
    }
}