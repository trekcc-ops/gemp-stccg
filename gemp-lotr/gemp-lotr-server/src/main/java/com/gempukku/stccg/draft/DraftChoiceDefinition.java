package com.gempukku.stccg.draft;

import com.gempukku.stccg.cards.CardCollection;
import com.gempukku.stccg.collection.DefaultCardCollection;

public interface DraftChoiceDefinition {
    Iterable<SoloDraft.DraftChoice> getDraftChoice(long seed, int stage, DefaultCardCollection draftPool);

    CardCollection getCardsForChoiceId(String choiceId, long seed, int stage);
}
