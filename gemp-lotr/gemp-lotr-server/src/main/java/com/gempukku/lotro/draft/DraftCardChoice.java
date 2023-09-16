package com.gempukku.lotro.draft;

import com.gempukku.lotro.game.CardCollection;

public interface DraftCardChoice {
    long getTimeLeft();

    CardCollection getCardCollection();
}
