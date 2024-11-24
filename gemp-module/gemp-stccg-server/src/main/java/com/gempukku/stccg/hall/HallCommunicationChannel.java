package com.gempukku.stccg.hall;

import com.gempukku.stccg.async.LongPollableResource;
import com.gempukku.stccg.async.WaitingRequest;
import com.gempukku.stccg.database.User;
import com.gempukku.stccg.tournament.TournamentQueue;
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
    public final synchronized void deregisterRequest() {
        _waitingRequest = null;
    }

    @Override
    public final synchronized boolean registerRequest(WaitingRequest waitingRequest) {
        if (_changed)
            return true;

        _waitingRequest = waitingRequest;
        return false;
    }

    public final synchronized void hallChanged() {
        _changed = true;
        if (_waitingRequest != null) {
            _waitingRequest.processRequest();
            _waitingRequest = null;
        }
    }

    public final int getChannelNumber() {
        return _channelNumber;
    }

    private void updateLastAccess() {
        _lastConsumed = System.currentTimeMillis();
    }

    public final long getLastAccessed() {
        return _lastConsumed;
    }

    public final void processCommunicationChannel(HallServer hallServer, final User player,
                                                  final HallChannelVisitor hallChannelVisitor) {
        updateLastAccess();

        hallChannelVisitor.channelNumber(_channelNumber);
        final MutableObject newDailyMessage = new MutableObject();

        final Map<String, Map<String, String>> tournamentQueuesOnServer = new LinkedHashMap<>();
        final Map<String, Map<String, String>> tablesOnServer = new LinkedHashMap<>();
        final Map<String, Map<String, String>> tournamentsOnServer = new LinkedHashMap<>();
        final Set<String> playedGamesOnServer = new HashSet<>();

        hallServer.processHall(player,
                new MyHallInfoVisitor(
                        hallChannelVisitor, newDailyMessage, tablesOnServer, tournamentQueuesOnServer,
                        tournamentsOnServer, playedGamesOnServer
                )
        );

        notifyAboutTournamentQueues(hallChannelVisitor, tournamentQueuesOnServer);
        _tournamentQueuePropsOnClient = tournamentQueuesOnServer;

        notifyAboutTournaments(hallChannelVisitor, tournamentsOnServer);
        _tournamentPropsOnClient = tournamentsOnServer;

        notifyAboutTables(hallChannelVisitor, tablesOnServer);
        _tablePropsOnClient = tablesOnServer;

        if (newDailyMessage.getValue() != null && !newDailyMessage.getValue().equals(_lastDailyMessage)) {
            String newDailyMessageString = (String) newDailyMessage.getValue();
            hallChannelVisitor.changedDailyMessage(newDailyMessageString);
            _lastDailyMessage = newDailyMessageString;
        }

        for (String gameId : playedGamesOnServer) {
            if (!_playedGames.contains(gameId))
                hallChannelVisitor.newPlayerGame(gameId);
        }
        _playedGames = playedGamesOnServer;

        _changed = false;
    }

    private void notifyAboutTables(HallChannelVisitor hallChannelVisitor,
                                   Map<String, Map<String, String>> tablesOnServer) {
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

    private void notifyAboutTournamentQueues(HallChannelVisitor hallChannelVisitor,
                                             Map<String, Map<String, String>> queues) {
        for (Map.Entry<String, Map<String, String>> tournamentQueueOnClient :
                _tournamentQueuePropsOnClient.entrySet()) {
            String tournamentQueueId = tournamentQueueOnClient.getKey();
            Map<String, String> tournamentProps = tournamentQueueOnClient.getValue();
            Map<String, String> tournamentLatestProps = queues.get(tournamentQueueId);
            if (tournamentLatestProps != null) {
                if (!tournamentProps.equals(tournamentLatestProps))
                    hallChannelVisitor.updateTournamentQueue(tournamentQueueId, tournamentLatestProps);
            } else {
                hallChannelVisitor.removeTournamentQueue(tournamentQueueId);
            }
        }

        for (Map.Entry<String, Map<String, String>> tournamentQueueOnServer : queues.entrySet())
            if (!_tournamentQueuePropsOnClient.containsKey(tournamentQueueOnServer.getKey()))
                hallChannelVisitor.addTournamentQueue(
                        tournamentQueueOnServer.getKey(), tournamentQueueOnServer.getValue());
    }

    private void notifyAboutTournaments(HallChannelVisitor hallChannelVisitor,
                                        Map<String, Map<String, String>> tournamentsOnServer) {
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

    private record MyHallInfoVisitor(HallChannelVisitor hallChannelVisitor, MutableObject newDailyMessage,
                                     Map<String, Map<String, String>> tablesOnServer,
                                     Map<String, Map<String, String>> tournamentQueuesOnServer,
                                     Map<String, Map<String, String>> tournamentsOnServer,
                                     Set<String> playedGamesOnServer) implements HallInfoVisitor {

        @Override
        public void serverTime(String time) {
            hallChannelVisitor.serverTime(time);
        }

        @Override
        public void setDailyMessage(String message) {
            newDailyMessage.setValue(message);
        }


        public void visitTable(GameTable table, String tableId, User user) {
            Map<String, String> serializedTable = table.serializeForUser(user);
            tablesOnServer.put(tableId, serializedTable);
        }

        @Override
        public void visitTournamentQueue(TournamentQueue queue, String tournamentQueueKey, String formatName,
                                         User user) {

            Map<String, String> props = new HashMap<>();
            props.put("cost", String.valueOf(queue.getCost()));
            props.put("collection", queue.getCollectionType().getFullName());
            props.put("format", formatName);
            props.put("queue", queue.getTournamentQueueName());
            props.put("playerCount", String.valueOf(queue.getPlayerCount()));
            props.put("prizes", queue.getPrizesDescription());
            props.put("system", queue.getPairingDescription());
            props.put("start", queue.getStartCondition());
            props.put("signedUp", String.valueOf(queue.isPlayerSignedUp(user.getName())));
            props.put("joinable", String.valueOf(queue.isJoinable()));

            tournamentQueuesOnServer.put(tournamentQueueKey, props);
        }

        @Override
        public void visitTournament(String tournamentKey, String collectionName, String formatName,
                                    String tournamentName, String pairingDescription, String tournamentStage,
                                    int round, int playerCount, boolean playerInCompetition) {
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
    }
}