package com.gempukku.stccg.tournament;

import com.gempukku.stccg.collection.CollectionType;

import java.util.Date;
import java.util.List;

public interface TournamentDAO {
    void addTournament(String tournamentId, String draftType, String tournamentName, String format,
                       CollectionType collectionType, Tournament.Stage stage, String pairingMechanism,
                       String prizeScheme, Date start);

    List<TournamentInfo> getUnfinishedTournaments();

    List<TournamentInfo> getFinishedTournamentsSince(long time);

    TournamentInfo getTournamentById(String tournamentId);

    void updateTournamentStage(String tournamentId, Tournament.Stage stage);

    void updateTournamentRound(String tournamentId, int round);

    List<TournamentQueueInfo> getFutureScheduledTournamentQueues(long tillDate);

    void updateScheduledTournamentStarted(String scheduledTournamentId);
}