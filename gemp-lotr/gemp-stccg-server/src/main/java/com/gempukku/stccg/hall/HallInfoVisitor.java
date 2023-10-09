package com.gempukku.stccg.hall;

import java.util.List;

public interface HallInfoVisitor {
    enum TableStatus {
        WAITING, PLAYING, FINISHED
    }

    void serverTime(String time);

    void motd(String motd);

    void visitTable(String tableId, String gameId, boolean watchable, TableStatus status, String statusDescription, String gameType, String formatName, String tournamentName, String userDesc, List<String> playerIds, boolean playing, boolean isPrivate, boolean isInviteOnly, String winner);

    void visitTournamentQueue(String tournamentQueueKey, int cost, String collectionName, String formatName, String tournamentQueueName, String tournamentPrizes,
                                     String pairingDescription, String startCondition, int playerCount, boolean playerSignedUp, boolean joinable);

    void visitTournament(String tournamentKey, String collectionName, String formatName, String tournamentName, String pairingDescription, String tournamentStage, int round, int playerCount, boolean playerInCompetition);

    void runningPlayerGame(String gameId);
}
