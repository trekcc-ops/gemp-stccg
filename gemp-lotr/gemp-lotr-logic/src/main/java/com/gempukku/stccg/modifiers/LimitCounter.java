package com.gempukku.stccg.modifiers;

public interface LimitCounter {
    int incrementToLimit(int limit, int incrementBy);

    int getUsedLimit();
}
