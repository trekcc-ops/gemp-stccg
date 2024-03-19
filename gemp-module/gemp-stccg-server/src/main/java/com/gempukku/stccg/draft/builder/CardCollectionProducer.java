package com.gempukku.stccg.draft.builder;

import com.gempukku.stccg.cards.CardCollection;

public interface CardCollectionProducer {
    CardCollection getCardCollection(long seed);
}
