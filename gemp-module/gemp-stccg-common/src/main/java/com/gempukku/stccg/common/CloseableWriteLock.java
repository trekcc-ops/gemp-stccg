package com.gempukku.stccg.common;

import java.util.concurrent.locks.ReadWriteLock;

public class CloseableWriteLock implements AutoCloseable {

    private final ReadWriteLock _readWriteLock;

    public CloseableWriteLock(ReadWriteLock readWriteLock) {
        _readWriteLock = readWriteLock;
    }

    public CloseableWriteLock open() {
        _readWriteLock.writeLock().lock();
        return this;
    }

    @Override
    public void close() {
        _readWriteLock.writeLock().unlock();
    }
}