package com.gempukku.stccg.tournament;

import com.gempukku.stccg.async.ServerObjects;
import com.gempukku.stccg.cards.CardBlueprintLibrary;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class TournamentQueueInfo {
    private final int _cost;
    private final long _startTime;
    private final String _tournamentName;
    private final String _scheduledTournamentId;
    private final String _format;
    private final String _playOffSystem;
    private final String _prizeScheme;
    private final int _minimumPlayers;

    public TournamentQueueInfo(String tournamentId, String tournamentName, String format, long startTime,
                               int cost, String playOffSystem, String prizeScheme, int minimumPlayers) {
        _scheduledTournamentId = tournamentId;
        _tournamentName = tournamentName;
        _format = format;
        _startTime = startTime;
        _cost = cost;
        _playOffSystem = playOffSystem;
        _prizeScheme = prizeScheme;
        _minimumPlayers = minimumPlayers;
    }

    public final String getScheduledTournamentId() {
        return _scheduledTournamentId;
    }

    public final int getCost() {
        return _cost;
    }

    public final long getStartTime() {
        return _startTime;
    }

    public final String getTournamentName() {
        return _tournamentName;
    }

    public final String getFormat() {
        return _format;
    }

    public final PairingMechanism getPairingMechanism() {
        return PairingMechanismRegistry.getPairingMechanism(_playOffSystem);
    }

    public final TournamentPrizes getPrizes(CardBlueprintLibrary library) {
        return TournamentPrizeSchemeRegistry.getTournamentPrizes(library, _prizeScheme);
    }

    public final int getMinimumPlayers() {
        return _minimumPlayers;
    }

    public final ScheduledTournamentQueue createNewScheduledTournamentQueue(ServerObjects objects, Tournament.Stage stage) {
        ZonedDateTime startDate = new Date(_startTime).toInstant().atZone(ZoneId.of("GMT"));
        String startCondition = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return new ScheduledTournamentQueue(this, _scheduledTournamentId, _startTime, _minimumPlayers,
                startCondition, _tournamentName, stage, objects);
    }

}