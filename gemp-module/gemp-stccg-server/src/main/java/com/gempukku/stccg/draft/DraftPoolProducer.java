package com.gempukku.stccg.draft;

import java.util.List;

public interface DraftPoolProducer {
    List<String> getDraftPool(long seed, long code);
}