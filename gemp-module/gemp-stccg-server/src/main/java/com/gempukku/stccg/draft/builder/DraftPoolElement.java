package com.gempukku.stccg.draft.builder;

import java.util.List;

public interface DraftPoolElement {
    String getDraftPoolType();

    List<List<String>> getDraftPackList();

    int getPacksToDraft();
}
