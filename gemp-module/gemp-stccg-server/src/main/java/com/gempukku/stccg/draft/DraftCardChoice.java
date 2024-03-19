package com.gempukku.stccg.draft;

import com.gempukku.stccg.cards.CardCollection;

public interface DraftCardChoice {
    long getTimeLeft();

    CardCollection getCardCollection();
}
