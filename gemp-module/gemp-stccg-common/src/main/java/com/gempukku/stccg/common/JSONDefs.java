package com.gempukku.stccg.common;

import java.util.*;

public class JSONDefs {
    public static class Pack {
        public enum PackType {
            SELECTION, PACK, RANDOM, RANDOM_FOIL, BOOSTER
        }

        public String Name;
        public PackType Type;
        public final boolean Recursive = false;
        public List<String> Items;
        public Map<String, String> Data;
    }

    public static class SealedTemplate {
        public String Name;
        public String ID;
        public String Format;
        public List<List<String>> SeriesProduct;
    }

    public static class ItemStub {
        public final String code;
        public final String name;
        public ItemStub(String c, String n) {
            code = c;
            name = n;
        }
    }

    public static class Format {
        public String game;
        public String code;
        public String name;
        public int order = 1000;
        public String surveyUrl;
        public String sites = "Fellowship";
        public boolean cancelRingBearerSkirmish = false;
        public boolean ruleOfFour = false;
        public boolean winAtEndOfRegroup = false;
        public boolean discardPileIsPublic = false;
        public boolean winOnControlling5Sites = false;
        public boolean playtest = false;
        public boolean validateShadowFPCount = true;
        public int minimumDrawDeckSize = 60;
        public final int maximumSeedDeckSize = 30;
        public final int missions = 6;
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

    }

    public static class FullFormatReadout {
        public Map<String, Format> Formats;
        public Map<String, SealedTemplate> SealedTemplates;
        public Map<String, ItemStub> DraftTemplates;
    }

    public static class ErrataInfo {
        public static final String PC_Errata = "PC";
        public String BaseID;
        public String Name;
        public String LinkText;
        public Map<String, String> ErrataIDs;
    }

    public static class PlayHistoryStats {
        public List<FormatStats> Stats;
        public int ActivePlayers;
        public int GamesCount;
        public String StartDate;
        public String EndDate;
    }

    public static class FormatStats {
        public String Format;
        public int Count;
        public boolean Casual;
    }
}
