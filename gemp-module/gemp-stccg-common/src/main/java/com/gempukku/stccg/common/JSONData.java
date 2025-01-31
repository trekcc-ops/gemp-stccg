package com.gempukku.stccg.common;

import java.util.*;

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

    public static class Format {
        public String gameType;
        public String code;
        public String name;
        public int order = 1000;
        public String surveyUrl;
        public boolean ruleOfFour = false;
        public boolean discardPileIsPublic = false;
        public boolean playtest = false;
        public int minimumDrawDeckSize = 60;
        public int maximumSeedDeckSize = 30;
        public int missions = 6;
        public int maximumSameName = 9999;
        public boolean mulliganRule = false;
        public ArrayList<Integer> set;
        public ArrayList<String> banned = new ArrayList<>();
        public ArrayList<String> restricted = new ArrayList<>();
        public ArrayList<String> valid = new ArrayList<>();
        public ArrayList<String> limit2 = new ArrayList<>();
        public ArrayList<String> limit3 = new ArrayList<>();
        public ArrayList<String> restrictedName = new ArrayList<>();
        public ArrayList<Integer> errataSets = new ArrayList<>();
        public Map<String, String> errata = new HashMap<>();
        public boolean hall = true;
        public boolean noShuffle = false;
        public boolean firstPlayerFixed = false;

    }

    @SuppressWarnings("unused") // Class is used in JSON data parsing, and usages may not be obvious to the IDE
    public static class FormatStats {
        public String Format;
        public int Count;
        public boolean Casual;
    }
}