package com.gempukku.stccg.draft2;

import com.gempukku.stccg.game.CardCollection;
import com.gempukku.stccg.game.DefaultCardCollection;
import java.util.List;

public interface SoloDraft {
    CardCollection initializeNewCollection(long seed);
    
    List<String> initializeDraftPool(long seed, long code);
    
    Iterable<DraftChoice> getAvailableChoices(long seed, int stage, DefaultCardCollection draftPool);

    CardCollection getCardsForChoiceId(String choiceId, long seed, int stage);

    boolean hasNextStage(long seed, int stage);

    String getCode();
    String getFormat();

    interface DraftChoice {
        String getChoiceId();
        String getBlueprintId();
        String getChoiceUrl();
    }
}
