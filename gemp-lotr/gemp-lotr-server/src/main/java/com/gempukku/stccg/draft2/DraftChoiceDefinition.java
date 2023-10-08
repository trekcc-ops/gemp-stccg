package com.gempukku.stccg.draft2;

import com.gempukku.stccg.game.CardCollection;
import com.gempukku.stccg.game.DefaultCardCollection;

public interface DraftChoiceDefinition {
    Iterable<SoloDraft.DraftChoice> getDraftChoice(long seed, int stage, DefaultCardCollection draftPool);

    CardCollection getCardsForChoiceId(String choiceId, long seed, int stage);
}
