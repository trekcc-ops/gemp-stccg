package com.gempukku.stccg.db;

import com.gempukku.stccg.DBDefs;
import com.gempukku.stccg.common.JSONDefs;

import java.time.ZonedDateTime;
import java.util.List;

public interface GameHistoryDAO {
    int addGameHistory(String winner, int winnerId, String loser, int loserId, String winReason, String loseReason, String winRecordingId, String loseRecordingId, String formatName, String tournament, String winnerDeckName, String loserDeckName, ZonedDateTime startDate, ZonedDateTime endDate, int version);
    DBDefs.GameHistory getGameHistory(String recordID);
    boolean doesReplayIDExist(String id);
    List<DBDefs.GameHistory> getGameHistoryForPlayer(User player, int start, int count);
    int getGameHistoryForPlayerCount(User player);

    List<DBDefs.GameHistory> getGameHistoryForFormat(String format, int count);

    int getActivePlayersCount(ZonedDateTime from, ZonedDateTime to);

    int getGamesPlayedCount(ZonedDateTime from, ZonedDateTime to);

    List<JSONDefs.FormatStats> GetAllGameFormatData(ZonedDateTime from, ZonedDateTime to);

    List<PlayerStatistic> getCasualPlayerStatistics(User player);

    List<PlayerStatistic> getCompetitivePlayerStatistics(User player);

}
