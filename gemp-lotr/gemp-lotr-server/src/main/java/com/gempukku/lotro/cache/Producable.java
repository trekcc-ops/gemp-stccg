package com.gempukku.lotro.cache;

public interface Producable<T, U> {
    U produce(T key);
}
