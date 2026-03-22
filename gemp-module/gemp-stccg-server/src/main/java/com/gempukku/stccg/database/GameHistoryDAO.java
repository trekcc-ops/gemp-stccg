package com.gempukku.stccg.database;

import com.gempukku.stccg.async.HttpProcessingException;
import com.gempukku.stccg.common.JSONData;

import java.time.ZonedDateTime;
import java.util.List;

public interface GameHistoryDAO {
    int addGameHistory(String winner, int winnerId, String loser, int loserId, String winReason, String loseReason,
                       String winRecordingId, String loseRecordingId, String formatName, String tournament,
                       String winnerDeckName, String loserDeckName, ZonedDateTime startDate, ZonedDateTime endDate,
                       int version);
    DBData.GameHistory getGameHistory(String recordID);
    boolean doesReplayIDExist(String id);
    List<DBData.GameHistory> getGameHistoryForPlayer(User player, int start, int count) throws HttpProcessingException;
    int getGameHistoryForPlayerCount(User player);

    List<DBData.GameHistory> getGameHistoryForFormat(String format, int count);

    int getActivePlayersCount(ZonedDateTime from, ZonedDateTime to);

    int getGamesPlayedCount(ZonedDateTime from, ZonedDateTime to);

    List<JSONData.FormatStats> GetAllGameFormatData(ZonedDateTime from, ZonedDateTime to);

    List<PlayerStatistic> getCasualPlayerStatistics(User player);

    List<PlayerStatistic> getCompetitivePlayerStatistics(User player);

}