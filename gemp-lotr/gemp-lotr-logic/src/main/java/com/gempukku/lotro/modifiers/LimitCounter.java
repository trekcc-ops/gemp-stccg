package com.gempukku.lotro.modifiers;

public interface LimitCounter {
    int incrementToLimit(int limit, int incrementBy);

    int getUsedLimit();
}
