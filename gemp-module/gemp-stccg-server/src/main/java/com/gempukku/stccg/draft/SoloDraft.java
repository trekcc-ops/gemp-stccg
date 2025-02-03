package com.gempukku.stccg.draft;

import com.gempukku.stccg.cards.CardBlueprintLibrary;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.DefaultCardCollection;
import com.gempukku.stccg.formats.FormatLibrary;

import java.util.List;

public interface SoloDraft {
    CardCollection initializeNewCollection(long seed);
    
    List<String> initializeDraftPool(long seed, long code);
    
    Iterable<DraftChoice> getAvailableChoices(long seed, int stage, DefaultCardCollection draftPool);

    CardCollection getCardsForChoiceId(String choiceId, long seed, int stage) throws InvalidDraftResultException;

    boolean hasNextStage(int stage);

    String getCode();
    String getFormat();

}