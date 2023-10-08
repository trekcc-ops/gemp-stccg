package com.gempukku.stccg.draft2.builder;

import java.util.ArrayList;
import java.util.List;


public class DefaultDraftPoolElement implements DraftPoolElement {
    private final List<ArrayList<String>> _draftPackList;
    private final String _draftPoolType;
    private final int _packsToDraft;
        
    public DefaultDraftPoolElement(String draftPoolType, List<ArrayList<String>> draftPackList, int packsToDraft) {
        _draftPoolType = draftPoolType;
        _draftPackList = draftPackList;
        _packsToDraft = packsToDraft;
    }

    @Override
    public String getDraftPoolType() {
        return _draftPoolType;
    }

    @Override
    public List<ArrayList<String>> getDraftPackList() {
        return (List) new ArrayList(_draftPackList);
    }

    @Override
    public int getPacksToDraft() {
        return _packsToDraft;
    }
}
