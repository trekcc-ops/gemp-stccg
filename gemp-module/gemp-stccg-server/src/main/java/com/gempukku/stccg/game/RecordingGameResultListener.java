package com.gempukku.stccg.game;

import java.util.Map;

public class RecordingGameResultListener implements GameResultListener {

    final GameParticipant[] _participants;
    final GameRecorder.GameRecordingInProgress _recording;

    public RecordingGameResultListener(GameParticipant[] participants, GameRecorder.GameRecordingInProgress recording) {
        _recording = recording;
        _participants = participants;
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
        _recording.finishRecording(_participants[0].getPlayerId(),
                "Game cancelled due to error", _participants[1].getPlayerId(),
                "Game cancelled due to error");
    }
}