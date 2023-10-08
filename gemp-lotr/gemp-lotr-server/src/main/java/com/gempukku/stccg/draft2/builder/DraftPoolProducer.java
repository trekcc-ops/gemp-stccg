package com.gempukku.stccg.draft2.builder;

import java.util.List;

public interface DraftPoolProducer {
    List<String> getDraftPool(long seed, long code);
}
