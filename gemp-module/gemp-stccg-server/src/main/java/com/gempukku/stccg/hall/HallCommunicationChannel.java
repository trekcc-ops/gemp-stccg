package com.gempukku.stccg.hall;

import com.gempukku.stccg.async.LongPollableResource;
import com.gempukku.stccg.async.WaitingRequest;
import com.gempukku.stccg.database.User;
import org.apache.commons.lang.mutable.MutableObject;

import java.util.*;

public class HallCommunicationChannel implements LongPollableResource {
    private final int _channelNumber;
    private long _lastConsumed;
    private String _lastDailyMessage;
    private final Map<String, Map<String, String>> _tournamentQueuePropsOnClient = new LinkedHashMap<>();
    private final Map<String, Map<String, String>> _tournamentPropsOnClient = new LinkedHashMap<>();
    private final Map<String, Map<String, String>> _tablePropsOnClient = new LinkedHashMap<>();
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
                                                  Map<Object, Object> itemsToAddToHallElem) {
        updateLastAccess();

        itemsToAddToHallElem.put("channelNumber", String.valueOf(_channelNumber));

        final Map<String, Map<String, String>> tournamentQueuesOnServer = new LinkedHashMap<>();
        final Map<String, Map<String, String>> tablesOnServer = new LinkedHashMap<>();
        final Map<String, Map<String, String>> tournamentsOnServer = new LinkedHashMap<>();
        final Set<String> playedGamesOnServer = new HashSet<>();

        hallServer.processHall(player, tournamentQueuesOnServer,
                playedGamesOnServer, tablesOnServer, tournamentsOnServer, itemsToAddToHallElem
        );

        getDifferences(_tournamentQueuePropsOnClient, tournamentQueuesOnServer, itemsToAddToHallElem, "queues");
        getDifferences(_tournamentPropsOnClient, tournamentsOnServer, itemsToAddToHallElem, "tournaments");
        getDifferences(_tablePropsOnClient, tablesOnServer, itemsToAddToHallElem, "tables");

        if (itemsToAddToHallElem.get("messageOfTheDay") instanceof String messageText)
                _lastDailyMessage = messageText;

        List<String> newGameIds = new ArrayList<>();
        for (String gameId : playedGamesOnServer) {
            if (!_playedGames.contains(gameId)) {
                newGameIds.add(gameId);
            }
        }
        itemsToAddToHallElem.put("newGameIds", newGameIds);

        _playedGames = playedGamesOnServer;

        _changed = false;
    }

    private void getDifferences(Map<String, Map<String, String>> clientProps,
                                Map<String, Map<String, String>> serverProps,
                                Map<Object, Object> itemsToAddToHallElem,
                                String label) {

        List<Map<String, String>> objectsToAdd = new ArrayList<>();

        for (Map.Entry<String, Map<String, String>> objectOnClient : clientProps.entrySet()) {
            String objectId = objectOnClient.getKey();
            Map<String, String> latestProps = serverProps.get(objectId);
            if (latestProps == null) {
                Map<String, String> objectPropMap = new HashMap<>();
                objectPropMap.put("action", "remove");
                objectPropMap.put("id", objectId);
                objectsToAdd.add(objectPropMap);
            } else if (!objectOnClient.getValue().equals(latestProps)) {
                Map<String, String> objectPropMap = new HashMap<>();
                objectPropMap.put("action", "update");
                objectPropMap.put("id", objectId);
                objectPropMap.putAll(latestProps);
                objectsToAdd.add(objectPropMap);
            }
        }
        for (Map.Entry<String, Map<String, String>> objectOnServer : serverProps.entrySet()) {
            if (!clientProps.containsKey(objectOnServer.getKey())) {
                Map<String, String> objectPropMap = new HashMap<>();
                objectPropMap.put("action", "add");
                objectPropMap.put("id", objectOnServer.getKey());
                objectPropMap.putAll(objectOnServer.getValue());
                objectsToAdd.add(objectPropMap);
            }
        }

        itemsToAddToHallElem.put(label, objectsToAdd);
        clientProps.clear();
        for (String key : serverProps.keySet()) {
            clientProps.put(key, serverProps.get(key));
        }

    }


}