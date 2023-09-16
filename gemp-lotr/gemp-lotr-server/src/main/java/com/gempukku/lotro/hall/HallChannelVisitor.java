package com.gempukku.lotro.hall;

import java.util.Map;

public interface HallChannelVisitor {
    void channelNumber(int channelNumber);
    void motdChanged(String motd);
    
    void serverTime(String serverTime);
    void newPlayerGame(String gameId);

    void addTournamentQueue(String queueId, Map<String, String> props);
    void updateTournamentQueue(String queueId, Map<String, String> props);
    void removeTournamentQueue(String queueId);

    void addTournament(String tournamentId, Map<String, String> props);
    void updateTournament(String tournamentId, Map<String, String> props);
    void removeTournament(String tournamentId);

    void addTable(String tableId, Map<String, String> props);
    void updateTable(String tableId, Map<String, String> props);
    void removeTable(String tableId);
}
