package com.gempukku.stccg.hall;

import com.gempukku.stccg.formats.GameFormat;

import java.util.List;

public interface HallInfoVisitor {
    enum TableStatus {
        WAITING, PLAYING, FINISHED
    }

    void serverTime(String time);

    void setDailyMessage(String message);

    void visitTable(String tableId, String gameId, boolean watchable, TableStatus status, String statusDescription,
                    GameTable table, String tournamentName, List<String> playerIds, boolean playing);

    void visitTable(String tableId, String gameId, boolean watchable, TableStatus status,
                    String statusDescription, String formatName,
                    String tournamentName, String userDesc, List<String> playerIds,
                    boolean playing, boolean isPrivate, boolean isInviteOnly, String winner,
                    GameFormat gameFormat);

    void visitTournamentQueue(String tournamentQueueKey, int cost, String collectionName, String formatName,
                              String tournamentQueueName, String tournamentPrizes, String pairingDescription,
                              String startCondition, int playerCount, boolean playerSignedUp, boolean joinable);

    void visitTournament(String tournamentKey, String collectionName, String formatName, String tournamentName,
                         String pairingDescription, String tournamentStage, int round, int playerCount,
                         boolean playerInCompetition);

    void runningPlayerGame(String gameId);
}