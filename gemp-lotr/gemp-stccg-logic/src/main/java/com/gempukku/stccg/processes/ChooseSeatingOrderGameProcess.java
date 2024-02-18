package com.gempukku.stccg.processes;

import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.PlayerOrder;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.PlayerOrderFeedback;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ChooseSeatingOrderGameProcess extends GameProcess {
    private final String[] _choices = new String[]{"first", "second", "third", "fourth", "fifth"};
    private final Map<String, Integer> _bids;
    private final PlayerOrderFeedback _playerOrderFeedback;

    private final Iterator<String> _biddingOrderPlayers;
    private final String[] _orderedPlayers;
    private boolean _sentBids;
    private DefaultGame _game;

    public ChooseSeatingOrderGameProcess(Map<String, Integer> bids, PlayerOrderFeedback playerOrderFeedback, DefaultGame game) {
        _game = game;
        _bids = bids;
        _playerOrderFeedback = playerOrderFeedback;

        ArrayList<String> participantList = new ArrayList<>(bids.keySet());
        Collections.shuffle(participantList, ThreadLocalRandom.current());

        participantList.sort((o1, o2) -> _bids.get(o2) - _bids.get(o1));

        _biddingOrderPlayers = participantList.iterator();
        _orderedPlayers = new String[participantList.size()];
    }

    @Override
    public void process() {
        if (!_sentBids) {
            _sentBids = true;
            for (Map.Entry<String, Integer> playerBid : _bids.entrySet())
                _game.getGameState().sendMessage(playerBid.getKey() + " bid " + playerBid.getValue());
        }
        checkForNextSeating(_game);
    }

    private int getLastEmptySeat() {
        boolean found = false;
        int emptySeatIndex = -1;
        for (int i = 0; i < _orderedPlayers.length; i++) {
            if (_orderedPlayers[i] == null) {
                if (found)
                    return -1;
                found = true;
                emptySeatIndex = i;
            }
        }
        return emptySeatIndex;
    }

    private void checkForNextSeating(DefaultGame game) {
        int lastEmptySeat = getLastEmptySeat();
        if (lastEmptySeat == -1)
            askNextPlayerToChoosePlace(game);
        else {
            _orderedPlayers[lastEmptySeat] = _biddingOrderPlayers.next();
            _playerOrderFeedback.setPlayerOrder(new PlayerOrder(Arrays.asList(_orderedPlayers)), _orderedPlayers[0]);
        }
    }

    private String[] getEmptySeatNumbers() {
        List<String> result = new LinkedList<>();
        for (int i = 0; i < _orderedPlayers.length; i++)
            if (_orderedPlayers[i] == null)
                result.add("Go " + _choices[i]);
        return result.toArray(new String[0]);
    }

    private void participantHasChosenSeat(DefaultGame game, String participant, int placeIndex) {
        _orderedPlayers[placeIndex] = participant;

        checkForNextSeating(game);
    }

    private void askNextPlayerToChoosePlace(final DefaultGame game) {
        final String playerId = _biddingOrderPlayers.next();
        game.getUserFeedback().sendAwaitingDecision(playerId,
                new MultipleChoiceAwaitingDecision(1, "Choose one", getEmptySeatNumbers()) {
                    @Override
                    protected void validDecisionMade(int index, String result) {
                        game.getGameState().sendMessage(playerId + " has chosen to go " + _choices[index]);
                        participantHasChosenSeat(game, playerId, index);
                    }
                }
        );
    }

    @Override
    public GameProcess getNextProcess() {
        return new PlayersDrawStartingHandGameProcess(_orderedPlayers[0], _game);
    }
}
