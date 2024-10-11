package com.gempukku.stccg.draft;

import com.gempukku.stccg.draft.builder.CardCollectionProducer;
import com.gempukku.stccg.draft.builder.DraftPoolProducer;
import com.gempukku.stccg.collection.CardCollection;
import com.gempukku.stccg.collection.DefaultCardCollection;

import java.util.List;

public class DefaultSoloDraft implements SoloDraft {
    private final String _code;
    private final String _format;
    private final CardCollectionProducer _newCollection;
    private final DraftPoolProducer _draftPool;
    private final List<? extends DraftChoiceDefinition> _draftChoiceDefinitions;

    public DefaultSoloDraft(String code, String format, CardCollectionProducer newCollection, List<? extends DraftChoiceDefinition> draftChoiceDefinitions, DraftPoolProducer draftPool) {
        _code = code;
        _format = format;
        _newCollection = newCollection;
        _draftPool = draftPool;
        _draftChoiceDefinitions = draftChoiceDefinitions;
    }

    @Override
    public CardCollection initializeNewCollection(long seed) {
        return (_newCollection != null) ? _newCollection.getCardCollection(seed) : null;
    }

    @Override
    public List<String> initializeDraftPool(long seed, long code) {
        return (_draftPool != null) ? _draftPool.getDraftPool(seed, code) : null;
    }

    @Override
    public Iterable<DraftChoice> getAvailableChoices(long seed, int stage, DefaultCardCollection draftPool) {
        return _draftChoiceDefinitions.get(stage).getDraftChoice(seed, stage, draftPool);
    }

    @Override
    public CardCollection getCardsForChoiceId(String choiceId, long seed, int stage) {
        return _draftChoiceDefinitions.get(stage).getCardsForChoiceId(choiceId, seed, stage);
    }

    @Override
    public boolean hasNextStage(int stage) {
        return stage + 1 < _draftChoiceDefinitions.size();
    }

    @Override
    public String getCode() {
        return _code;
    }

    @Override
    public String getFormat() {
        return _format;
    }
}