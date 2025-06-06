package com.gempukku.stccg.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MultipleComparator<T> implements Comparator<T>, Serializable {
    private final List<Comparator<T>> _comparators = new ArrayList<>();

    @SafeVarargs
    public MultipleComparator(Comparator<T>... comparators) {
        Collections.addAll(_comparators, comparators);
    }

    public void addComparator(Comparator<T> comparator) {
        _comparators.add(comparator);
    }

    @Override
    public int compare(T o1, T o2) {
        for (Comparator<T> comparator : _comparators) {
            int result = comparator.compare(o1, o2);
            if (result != 0)
                return result;
        }
        return 0;
    }
}