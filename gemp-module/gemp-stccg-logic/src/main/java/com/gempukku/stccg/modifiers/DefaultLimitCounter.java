package com.gempukku.stccg.modifiers;

public class DefaultLimitCounter implements LimitCounter {
    private int _count = 0;

    @Override
    public int incrementToLimit(int limit, int incrementBy) {
        int maxIncrement = limit - _count;
        int finalIncrement = Math.min(maxIncrement, incrementBy);
        _count += finalIncrement;
        return finalIncrement;
    }

    @Override
    public int getUsedLimit() {
        return _count;
    }

    public void countUse() { _count++; }
}