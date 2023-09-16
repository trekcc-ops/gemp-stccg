package com.gempukku.lotro.cards;

import com.gempukku.lotro.cards.lotronly.LotroPhysicalCard;

public interface PhysicalCardVisitor {
    boolean visitPhysicalCard(LotroPhysicalCard physicalCard);
}
