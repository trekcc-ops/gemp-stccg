package com.gempukku.stccg.async;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class LongPollingSystem {
    private final Set<ResourceWaitingRequest> _waitingActions = Collections.synchronizedSet(new HashSet<>());

    private final ExecutorService _executorService = new ThreadPoolExecutor(10, Integer.MAX_VALUE,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<>());

    public void start() {
        ProcessingRunnable _timeoutRunnable = new ProcessingRunnable();
        Thread thr = new Thread(_timeoutRunnable);
        thr.start();
    }

    public void processLongPollingResource(LongPollingResource resource, LongPollableResource pollableResource) {
        ResourceWaitingRequest request = new ResourceWaitingRequest(pollableResource, resource, System.currentTimeMillis());
        if (pollableResource.registerRequest(request)) {
            execute(resource);
        } else {
            _waitingActions.add(request);
        }
    }

    private void pause() {
        try {
            long _pollingInterval = 100;
            Thread.sleep(_pollingInterval);
        } catch (InterruptedException exp) {
            // Ignore
        }
    }

    private void processWaitingRequest(final ResourceWaitingRequest request) {
        _waitingActions.remove(request);
        execute(request.getLongPollingResource());
    }

    private void execute(final LongPollingResource resource) {
        _executorService.submit(resource::processIfNotProcessed);
    }

    private class ProcessingRunnable implements Runnable {
        @Override
        public void run() {
            while (true) {
                Set<ResourceWaitingRequest> resourcesCopy;
                synchronized (_waitingActions) {
                    resourcesCopy = new HashSet<>(_waitingActions);
                }

                long now = System.currentTimeMillis();
                for (ResourceWaitingRequest waitingRequest : resourcesCopy) {
                    if (waitingRequest.getLongPollingResource().wasProcessed())
                        _waitingActions.remove(waitingRequest);
                    else {
                        long _pollingLength = 5000;
                        if (waitingRequest.getStart() + _pollingLength < now) {
                            waitingRequest.getLongPollableResource().deregisterRequest(waitingRequest);
                            _waitingActions.remove(waitingRequest);
                            execute(waitingRequest.getLongPollingResource());
                        }
                    }
                }

                pause();
            }
        }
    }

    private class ResourceWaitingRequest implements WaitingRequest {
        private final LongPollingResource _longPollingResource;
        private final LongPollableResource _longPollableResource;
        private final long _start;

        private ResourceWaitingRequest(LongPollableResource longPollableResource, LongPollingResource longPollingResource, long start) {
            _longPollableResource = longPollableResource;
            _longPollingResource = longPollingResource;
            _start = start;
        }

        @Override
        public void processRequest() {
            processWaitingRequest(this);
        }

        public LongPollableResource getLongPollableResource() {
            return _longPollableResource;
        }

        public LongPollingResource getLongPollingResource() {
            return _longPollingResource;
        }

        public long getStart() {
            return _start;
        }
    }
}
