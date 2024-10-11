package com.gempukku.stccg.draft.builder;

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
    public String getDraftPoolType() {
        return _draftPoolType;
    }

    @Override
    public List<List<String>> getDraftPackList() {
        return _draftPackList;
    }

    @Override
    public int getPacksToDraft() {
        return _packsToDraft;
    }
}
