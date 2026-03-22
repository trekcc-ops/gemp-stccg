package com.gempukku.stccg.common;

public class JSONData {

    @SuppressWarnings("unused") // Class is used in JSON data parsing, and usages may not be obvious to the IDE
    public static class ItemStub {
        public final String code;
        public final String name;
        public ItemStub(String c, String n) {
            code = c;
            name = n;
        }
    }

    @SuppressWarnings("unused") // Class is used in JSON data parsing, and usages may not be obvious to the IDE
    public static class FormatStats {
        public String Format;
        public int Count;
        public boolean Casual;
    }
}