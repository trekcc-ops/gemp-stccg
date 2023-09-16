package com.gempukku.polling;

public interface LongPollingResource {
    boolean wasProcessed();

    void processIfNotProcessed();
}
