package com.gempukku.stccg.draft;

import com.gempukku.stccg.async.LongPollableResource;
import com.gempukku.stccg.async.WaitingRequest;

public class DraftCommunicationChannel implements LongPollableResource {
    private final int _channelNumber;

    private volatile boolean _changed;
    private volatile WaitingRequest _waitingRequest;

    public DraftCommunicationChannel(int channelNumber) {
        _channelNumber = channelNumber;
    }

    public int getChannelNumber() {
        return _channelNumber;
    }

    @Override
    public synchronized void deregisterRequest() {
        _waitingRequest = null;
    }

    @Override
    public boolean registerRequest(WaitingRequest waitingRequest) {
        if (_changed)
            return true;

        _waitingRequest = waitingRequest;
        return false;
    }

    public synchronized void draftChanged() {
        _changed = true;
        if (_waitingRequest != null) {
            _waitingRequest.processRequest();
            _waitingRequest = null;
        }
    }

}
