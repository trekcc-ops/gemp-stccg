package com.gempukku.stccg.game;

import java.util.HashMap;
import java.util.Map;

public class SnapshotData {

    private final Map<Snapshotable, Snapshotable> snapshotableMap = new HashMap<>();

    /**
     * Gets the snapshotable to store in the snapshot given a snapshotable. This will return the same snapshotable
     * if a snapshot of it is already in the snapshot data, otherwise a new snapshot is taken and returned.
     * @param data the snapshotable
     */
    public <T extends Snapshotable> T getDataForSnapshot(T data) {
        if (data == null) {
            return null;
        }
        Snapshotable dataToReturn = snapshotableMap.get(data);
        if (dataToReturn == null) {
            dataToReturn = data.generateSnapshot(this);
            snapshotableMap.put(data, dataToReturn);
            snapshotableMap.put(dataToReturn, dataToReturn);
        }
        return (T) dataToReturn;
    }

}