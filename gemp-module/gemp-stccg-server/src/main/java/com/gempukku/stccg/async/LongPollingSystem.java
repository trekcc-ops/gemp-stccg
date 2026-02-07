package com.gempukku.stccg.async;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class LongPollingSystem {
    private final static long POLLING_LENGTH = 5000;
    private final Set<ResourceWaitingRequest> _waitingActions = Collections.synchronizedSet(new HashSet<>());

    private final ExecutorService _executorService = new ThreadPoolExecutor(10, Integer.MAX_VALUE,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<>());

    public LongPollingSystem() {
        ProcessingRunnable _timeoutRunnable = new ProcessingRunnable();
        Thread thr = new Thread(_timeoutRunnable);
        thr.start();
    }

    public final void processLongPollingResource(LongPollingResource resource, LongPollableResource pollableResource) {
        ResourceWaitingRequest request =
                new ResourceWaitingRequest(pollableResource, resource, System.currentTimeMillis());
        if (pollableResource.registerRequest(request)) {
            execute(resource);
        } else {
            _waitingActions.add(request);
        }
    }

    private void execute(final LongPollingResource resource) {
        _executorService.submit(resource::processIfNotProcessed);
    }

    private class ProcessingRunnable implements Runnable {
        @SuppressWarnings("InfiniteLoopStatement")
        @Override
        public final void run() {
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
                        if (waitingRequest.getStart() + POLLING_LENGTH < now) {
                            waitingRequest.getLongPollableResource().deregisterRequest();
                            _waitingActions.remove(waitingRequest);
                            execute(waitingRequest.getLongPollingResource());
                        }
                    }
                }

                pause();
            }
        }

        private static void pause() {
            try {
                long _pollingInterval = 100;
                Thread.sleep(_pollingInterval);
            } catch (InterruptedException exp) {
                // Ignore
            }
        }
    }

    private class ResourceWaitingRequest implements WaitingRequest {
        private final LongPollingResource _longPollingResource;
        private final LongPollableResource _longPollableResource;
        private final long _start;

        private ResourceWaitingRequest(LongPollableResource longPollableResource,
                                       LongPollingResource longPollingResource, long start) {
            _longPollableResource = longPollableResource;
            _longPollingResource = longPollingResource;
            _start = start;
        }

        @Override
        public final void processRequest() {
            _waitingActions.remove(this);
            execute(_longPollingResource);
        }

        final LongPollableResource getLongPollableResource() {
            return _longPollableResource;
        }

        final LongPollingResource getLongPollingResource() {
            return _longPollingResource;
        }

        final long getStart() {
            return _start;
        }
    }
}