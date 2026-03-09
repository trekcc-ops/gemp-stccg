package com.gempukku.stccg.cards.physicalcard;

public interface StoppableCard extends PhysicalCard {

    boolean isStopped();
    void stop();
}