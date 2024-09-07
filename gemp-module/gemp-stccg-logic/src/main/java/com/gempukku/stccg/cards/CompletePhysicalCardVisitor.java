package com.gempukku.stccg.cards;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCardVisitor;

public abstract class CompletePhysicalCardVisitor implements PhysicalCardVisitor {
    @Override
    public boolean visitPhysicalCard(PhysicalCard physicalCard) {
        doVisitPhysicalCard(physicalCard);
        return false;
    }

    protected abstract void doVisitPhysicalCard(PhysicalCard physicalCard);
}
