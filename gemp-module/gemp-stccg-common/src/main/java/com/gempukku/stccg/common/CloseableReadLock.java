package com.gempukku.stccg.common;

import java.util.concurrent.locks.ReadWriteLock;

public class CloseableReadLock implements AutoCloseable {

    private final ReadWriteLock _readWriteLock;

    public CloseableReadLock(ReadWriteLock readWriteLock) {
        _readWriteLock = readWriteLock;
    }

    public CloseableReadLock open() {
        _readWriteLock.readLock().lock();
        return this;
    }

    @Override
    public void close() {
        _readWriteLock.readLock().unlock();
    }
}