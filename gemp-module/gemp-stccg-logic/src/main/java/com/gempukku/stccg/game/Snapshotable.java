package com.gempukku.stccg.game;

public interface Snapshotable<T extends Snapshotable> {

    T generateSnapshot(SnapshotData snapshotData);

}