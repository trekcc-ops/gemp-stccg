package com.gempukku.stccg.draft;

import com.gempukku.stccg.game.CardCollection;

import java.util.List;

public class DraftPack {
    public final CardCollection _fixedCollection;
    public final List<String> _packs;

    public DraftPack(CardCollection fixedCollection, List<String> packs) {
        _fixedCollection = fixedCollection;
        _packs = packs;
    }

    public CardCollection getFixedCollection() {
        return _fixedCollection;
    }

    public List<String> getPacks() {
        return _packs;
    }
}
