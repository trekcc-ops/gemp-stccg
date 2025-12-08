package com.gempukku.stccg.modifiers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DefaultLimitCounter implements LimitCounter {

    @JsonProperty("count")
    private int _count;

    @Override
    public int incrementToLimit(int limit, int incrementBy) {
        int maxIncrement = limit - _count;
        int finalIncrement = Math.min(maxIncrement, incrementBy);
        _count += finalIncrement;
        return finalIncrement;
    }

    @JsonIgnore
    @Override
    public int getUsedLimit() {
        return _count;
    }

    public void countUse() { _count++; }
}