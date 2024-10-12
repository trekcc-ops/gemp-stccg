package com.gempukku.stccg.draft;

import java.util.List;


public class DefaultDraftPoolElement implements DraftPoolElement {
    private final List<List<String>> _draftPackList;
    private final String _draftPoolType;
    private final int _packsToDraft;
        
    public DefaultDraftPoolElement(String draftPoolType, List<List<String>> draftPackList, int packsToDraft) {
        _draftPoolType = draftPoolType;
        _draftPackList = draftPackList;
        _packsToDraft = packsToDraft;
    }

    @Override
    public final String getDraftPoolType() {
        return _draftPoolType;
    }

    @Override
    public final List<List<String>> getDraftPackList() {
        return _draftPackList;
    }

    @Override
    public final int getPacksToDraft() {
        return _packsToDraft;
    }
}