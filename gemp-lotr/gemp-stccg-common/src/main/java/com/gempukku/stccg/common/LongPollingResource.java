package com.gempukku.stccg.common;

public interface LongPollingResource {
    boolean wasProcessed();

    void processIfNotProcessed();
}
