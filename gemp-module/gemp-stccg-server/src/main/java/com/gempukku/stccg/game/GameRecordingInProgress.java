package com.gempukku.stccg.game;

public interface GameRecordingInProgress {
    void finishRecording(String winner, String winReason, String loser, String loseReason);
}