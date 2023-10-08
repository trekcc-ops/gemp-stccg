package com.gempukku.stccg.draft2.builder;

import com.gempukku.stccg.game.CardCollection;

public interface CardCollectionProducer {
    CardCollection getCardCollection(long seed);
}
