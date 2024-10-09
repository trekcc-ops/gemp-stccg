package com.gempukku.stccg.hall;

import com.gempukku.stccg.db.User;
import com.gempukku.stccg.async.LongPollableResource;
import com.gempukku.stccg.async.WaitingRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableObject;

import java.util.*;

public class HallCommunicationChannel implements LongPollableResource {
    private final int _channelNumber;
    private long _lastConsumed;
    private String _lastDailyMessage;
    private Map<String, Map<String, String>> _tournamentQueuePropsOnClient = new LinkedHashMap<>();
    private Map<String, Map<String, String>> _tournamentPropsOnClient = new LinkedHashMap<>();
    private Map<String, Map<String, String>> _tablePropsOnClient = new LinkedHashMap<>();
    private Set<String> _playedGames = new HashSet<>();
    private volatile boolean _changed;
    private volatile WaitingRequest _waitingRequest;

    public HallCommunicationChannel(int channelNumber) {
        _channelNumber = channelNumber;
    }

    @Override
    public synchronized void deregisterRequest() {
        _waitingRequest = null;
    }

    @Override
    public synchronized boolean registerRequest(WaitingRequest waitingRequest) {
        if (_changed)
            return true;

        _waitingRequest = waitingRequest;
        return false;
    }

    public synchronized void hallChanged() {
        _changed = true;
        if (_waitingRequest != null) {
            _waitingRequest.processRequest();
            _waitingRequest = null;
        }
    }

    public int getChannelNumber() {
        return _channelNumber;
    }

    private void updateLastAccess() {
        _lastConsumed = System.currentTimeMillis();
    }

    public long getLastAccessed() {
        return _lastConsumed;
    }

    public void processCommunicationChannel(HallServer hallServer, final User player, final HallChannelVisitor hallChannelVisitor) {
        updateLastAccess();

        hallChannelVisitor.channelNumber(_channelNumber);
        final MutableObject newDailyMessage = new MutableObject();

        final Map<String, Map<String, String>> tournamentQueuesOnServer = new LinkedHashMap<>();
        final Map<String, Map<String, String>> tablesOnServer = new LinkedHashMap<>();
        final Map<String, Map<String, String>> tournamentsOnServer = new LinkedHashMap<>();
        final Set<String> playedGamesOnServer = new HashSet<>();

        hallServer.processHall(player,
                new HallInfoVisitor() {
                    @Override
                    public void serverTime(String time) {
                        hallChannelVisitor.serverTime(time);
                    }

                    @Override
                    public void setDailyMessage(String message) {
                        newDailyMessage.setValue(message);
                    }

                    @Override
                    public void visitTable(String tableId, String gameId, boolean watchable, TableStatus status,
                                           String statusDescription, String gameType, String formatName,
                                           String tournamentName, String userDesc, List<String> playerIds,
                                           boolean playing, boolean isPrivate, boolean isInviteOnly, String winner) {
                        Map<String, String> props = new HashMap<>();
                        props.put("gameId", gameId);
                        props.put("watchable", String.valueOf(watchable));
                        props.put("status", String.valueOf(status));
                        props.put("statusDescription", statusDescription);
                        props.put("gameType", gameType);
                        props.put("format", formatName);
                        props.put("userDescription", userDesc);
                        props.put("isPrivate", String.valueOf(isPrivate));
                        props.put("isInviteOnly", String.valueOf(isInviteOnly));
                        props.put("tournament", tournamentName);
                        props.put("players", StringUtils.join(playerIds, ","));
                        props.put("playing", String.valueOf(playing));
                        if (winner != null)
                            props.put("winner", winner);

                        tablesOnServer.put(tableId, props);
                    }

                    @Override
                    public void visitTournamentQueue(String tournamentQueueKey, int cost, String collectionName, String formatName, String tournamentQueueName,
                                                     String tournamentPrizes, String pairingDescription, String startCondition, int playerCount, boolean playerSignedUp, boolean joinable) {
                        Map<String, String> props = new HashMap<>();
                        props.put("cost", String.valueOf(cost));
                        props.put("collection", collectionName);
                        props.put("format", formatName);
                        props.put("queue", tournamentQueueName);
                        props.put("playerCount", String.valueOf(playerCount));
                        props.put("prizes", tournamentPrizes);
                        props.put("system", pairingDescription);
                        props.put("start", startCondition);
                        props.put("signedUp", String.valueOf(playerSignedUp));
                        props.put("joinable", String.valueOf(joinable));

                        tournamentQueuesOnServer.put(tournamentQueueKey, props);
                    }

                    @Override
                    public void visitTournament(String tournamentKey, String collectionName, String formatName, String tournamentName, String pairingDescription,
                                                String tournamentStage, int round, int playerCount, boolean playerInCompetition) {
                        Map<String, String> props = new HashMap<>();
                        props.put("collection", collectionName);
                        props.put("format", formatName);
                        props.put("name", tournamentName);
                        props.put("system", pairingDescription);
                        props.put("stage", tournamentStage);
                        props.put("round", String.valueOf(round));
                        props.put("playerCount", String.valueOf(playerCount));
                        props.put("signedUp", String.valueOf(playerInCompetition));

                        tournamentsOnServer.put(tournamentKey, props);
                    }

                    @Override
                    public void runningPlayerGame(String gameId) {
                        playedGamesOnServer.add(gameId);
                    }
                });

        notifyAboutTournamentQueues(hallChannelVisitor, tournamentQueuesOnServer);
        _tournamentQueuePropsOnClient = tournamentQueuesOnServer;

        notifyAboutTournaments(hallChannelVisitor, tournamentsOnServer);
        _tournamentPropsOnClient = tournamentsOnServer;

        notifyAboutTables(hallChannelVisitor, tablesOnServer);
        _tablePropsOnClient = tablesOnServer;

        if (newDailyMessage.getValue() != null && !newDailyMessage.getValue().equals(_lastDailyMessage)) {
            String newMotdStr = (String) newDailyMessage.getValue();
            hallChannelVisitor.changedDailyMessage(newMotdStr);
            _lastDailyMessage = newMotdStr;
        }

        for (String gameId : playedGamesOnServer) {
            if (!_playedGames.contains(gameId))
                hallChannelVisitor.newPlayerGame(gameId);
        }
        _playedGames = playedGamesOnServer;

        _changed = false;
    }

    private void notifyAboutTables(HallChannelVisitor hallChannelVisitor, Map<String, Map<String, String>> tablesOnServer) {
        for (Map.Entry<String, Map<String, String>> tableOnClient : _tablePropsOnClient.entrySet()) {
            String tableId = tableOnClient.getKey();
            Map<String, String> tableProps = tableOnClient.getValue();
            Map<String, String> tableLatestProps = tablesOnServer.get(tableId);
            if (tableLatestProps != null) {
                if (!tableProps.equals(tableLatestProps))
                    hallChannelVisitor.updateTable(tableId, tableLatestProps);
            } else {
                hallChannelVisitor.removeTable(tableId);
            }
        }

        for (Map.Entry<String, Map<String, String>> tableOnServer : tablesOnServer.entrySet())
            if (!_tablePropsOnClient.containsKey(tableOnServer.getKey()))
                hallChannelVisitor.addTable(tableOnServer.getKey(), tableOnServer.getValue());
    }

    private void notifyAboutTournamentQueues(HallChannelVisitor hallChannelVisitor, Map<String, Map<String, String>> tournamentQueuesOnServer) {
        for (Map.Entry<String, Map<String, String>> tournamentQueueOnClient : _tournamentQueuePropsOnClient.entrySet()) {
            String tournamentQueueId = tournamentQueueOnClient.getKey();
            Map<String, String> tournamentProps = tournamentQueueOnClient.getValue();
            Map<String, String> tournamentLatestProps = tournamentQueuesOnServer.get(tournamentQueueId);
            if (tournamentLatestProps != null) {
                if (!tournamentProps.equals(tournamentLatestProps))
                    hallChannelVisitor.updateTournamentQueue(tournamentQueueId, tournamentLatestProps);
            } else {
                hallChannelVisitor.removeTournamentQueue(tournamentQueueId);
            }
        }

        for (Map.Entry<String, Map<String, String>> tournamentQueueOnServer : tournamentQueuesOnServer.entrySet())
            if (!_tournamentQueuePropsOnClient.containsKey(tournamentQueueOnServer.getKey()))
                hallChannelVisitor.addTournamentQueue(tournamentQueueOnServer.getKey(), tournamentQueueOnServer.getValue());
    }

    private void notifyAboutTournaments(HallChannelVisitor hallChannelVisitor, Map<String, Map<String, String>> tournamentsOnServer) {
        for (Map.Entry<String, Map<String, String>> tournamentOnClient : _tournamentPropsOnClient.entrySet()) {
            String tournamentId = tournamentOnClient.getKey();
            Map<String, String> tournamentProps = tournamentOnClient.getValue();
            Map<String, String> tournamentLatestProps = tournamentsOnServer.get(tournamentId);
            if (tournamentLatestProps != null) {
                if (!tournamentProps.equals(tournamentLatestProps))
                    hallChannelVisitor.updateTournament(tournamentId, tournamentLatestProps);
            } else {
                hallChannelVisitor.removeTournament(tournamentId);
            }
        }

        for (Map.Entry<String, Map<String, String>> tournamentQueueOnServer : tournamentsOnServer.entrySet())
            if (!_tournamentPropsOnClient.containsKey(tournamentQueueOnServer.getKey()))
                hallChannelVisitor.addTournament(tournamentQueueOnServer.getKey(), tournamentQueueOnServer.getValue());
    }
}