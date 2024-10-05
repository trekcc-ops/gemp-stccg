package com.gempukku.stccg.async;

public interface LongPollableResource {
    /**
     * Registers the request for changes, however if there are any changes that can be consumed immediatelly, then
     * true is returned.
     * @param waitingRequest
     * @return
     */
    boolean registerRequest(WaitingRequest waitingRequest);

    void deregisterRequest();
}
