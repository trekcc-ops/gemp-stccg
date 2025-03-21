package com.gempukku.stccg.common;

import java.io.Serializable;
import java.util.Comparator;

public class DescComparator<T> implements Comparator<T>, Serializable {
    private final Comparator<T> _comparator;

    public DescComparator(Comparator<T> comparator) {
        _comparator = comparator;
    }

    @Override
    public int compare(T o1, T o2) {
        return _comparator.compare(o2, o1);
    }
}