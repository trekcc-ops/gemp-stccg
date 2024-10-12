package com.gempukku.stccg.draft;

import com.gempukku.stccg.collection.CardCollection;

public interface CardCollectionProducer {
    CardCollection getCardCollection(long seed);
}