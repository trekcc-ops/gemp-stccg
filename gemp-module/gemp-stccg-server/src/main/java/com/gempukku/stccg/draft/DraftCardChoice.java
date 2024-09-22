package com.gempukku.stccg.draft;

import com.gempukku.stccg.collection.CardCollection;

public interface DraftCardChoice {
    long getTimeLeft();

    CardCollection getCardCollection();
}
