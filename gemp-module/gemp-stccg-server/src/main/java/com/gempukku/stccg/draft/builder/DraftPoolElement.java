package com.gempukku.stccg.draft.builder;

import java.util.ArrayList;
import java.util.List;

public interface DraftPoolElement {
    String getDraftPoolType();

    List<ArrayList<String>> getDraftPackList();

    int getPacksToDraft();
}
