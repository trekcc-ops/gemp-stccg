package com.gempukku.stccg.serializing;

import java.util.HashMap;
import java.util.Map;

public class SerializingLibrary {

    Map<Integer, String> numberNameMap = new HashMap<>();

    SerializingLibrary() {
        numberNameMap.put(5, "five");
        numberNameMap.put(6, "six");
        numberNameMap.put(11, "eleven");
    }

    public String get(int num) {
        return numberNameMap.get(num);
    }

}