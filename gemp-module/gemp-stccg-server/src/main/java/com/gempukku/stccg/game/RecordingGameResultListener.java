package com.gempukku.stccg.game;

import java.util.Map;
import java.util.Set;

public class RecordingGameResultListener implements GameResultListener {

    final String[] _playerNames;
    final GameRecordingInProgress _recording;

    public RecordingGameResultListener(Set<String> playerNames, GameRecordingInProgress recording) {
        _recording = recording;
        _playerNames = playerNames.toArray(new String[0]);
    }
    @Override
    public void gameFinished(String winnerPlayerId, String winReason, Map<String, String> loserReasons) {
        final var loserEntry = loserReasons.entrySet().iterator().next();

        //potentially this is where to kick off any "reveal deck" events
        //gameMediator.readoutParticipantDecks();
        _recording.finishRecording(
                winnerPlayerId, winReason, loserEntry.getKey(), loserEntry.getValue());
    }

    @Override
    public void gameCancelled() {
        _recording.finishRecording(_playerNames[0], "Game cancelled due to error",
                _playerNames[1], "Game cancelled due to error");
    }
}