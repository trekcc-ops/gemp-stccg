package com.gempukku.stccg.draft;

import com.gempukku.stccg.game.CardCollection;

public interface DraftCardChoice {
    long getTimeLeft();

    CardCollection getCardCollection();
}
