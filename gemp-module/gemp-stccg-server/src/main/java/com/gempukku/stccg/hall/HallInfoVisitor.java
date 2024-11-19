package com.gempukku.stccg.hall;

import com.gempukku.stccg.database.User;
import com.gempukku.stccg.tournament.TournamentQueue;

public interface HallInfoVisitor {

    void serverTime(String time);

    void setDailyMessage(String message);

    void visitTable(GameTable table, String tableId, User user);

    void visitTournamentQueue(TournamentQueue queue, String tournamentQueueKey, String formatName, User user);

    void visitTournament(String tournamentKey, String collectionName, String formatName, String tournamentName,
                         String pairingDescription, String tournamentStage, int round, int playerCount,
                         boolean playerInCompetition);

    void runningPlayerGame(String gameId);
}