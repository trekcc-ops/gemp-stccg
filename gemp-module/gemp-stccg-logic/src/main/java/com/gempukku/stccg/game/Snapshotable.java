package com.gempukku.stccg.game;

public interface Snapshotable<T extends Snapshotable> {

    /**
     * Generates a snapshot of the snapshotable.
     * @param selfSnapshot the snapshot object
     * @param snapshotData snapshot data
     */
    void generateSnapshot(T selfSnapshot, SnapshotData snapshotData);

}
