package com.gempukku.stccg.draft.builder;

import com.gempukku.stccg.collection.CardCollection;

public interface CardCollectionProducer {
    CardCollection getCardCollection(long seed);
}
