package com.gempukku.stccg.async;

public interface LongPollableResource {
    /* Registers the request for changes, however if there are any changes that can be consumed immediately, then
        true is returned. */
    boolean registerRequest(WaitingRequest waitingRequest);

    void deregisterRequest();
}