package com.gempukku.stccg.cardparsing;

import java.util.*;

public class StripSentencesTest extends NewLibraryTest {

    private String stripIcons(String originalSentence) {
        String modifiedSentence = originalSentence;
        List<String> icons = new ArrayList();
        icons.add("[fed]");
        icons.add("[fer]");
        icons.add("[tng]");

        for (String string : icons) {
            modifiedSentence = modifiedSentence.replace(string, "").strip();
        }

        return modifiedSentence;
    }

    private String stripLocations(String originalSentence) {
        String modifiedSentence = originalSentence;
        List<String> locations = new ArrayList();
        locations.add("there");
        locations.add("veytan");
        locations.add("here");
        locations.add("calder ii");
        locations.add("on table");

        for (String string : locations) {
            modifiedSentence = modifiedSentence.replace(string, "").strip();
        }

        return modifiedSentence;
    }

    private String stripVerbs(String originalSentence) {
        String modifiedSentence = originalSentence;
        List<String> usageLimits = new ArrayList();
        usageLimits.add("attempt");
        usageLimits.add("discard"); // Can also refer to "discard pile"
        usageLimits.add("download");
        usageLimits.add("place");
        usageLimits.add("play");
        usageLimits.add("report");
        usageLimits.add("seed");
        usageLimits.add("solve");
        usageLimits.add("steal");
        usageLimits.add("stop");

        for (String string : usageLimits) {
            String repeat = "re" + string;
            String repeatWithDash = "re-" + string;
            String thirdPerson = string + "s";
            String pastTense = string + "ed";
            String gerund = string + "ing";
            modifiedSentence = modifiedSentence.replace(repeat, "").strip();
            modifiedSentence = modifiedSentence.replace(repeatWithDash, "").strip();
            modifiedSentence = modifiedSentence.replace(pastTense, "").strip();
            modifiedSentence = modifiedSentence.replace(thirdPerson, "").strip();
            modifiedSentence = modifiedSentence.replace(gerund, "").strip();
            modifiedSentence = modifiedSentence.replace(string, "").strip();
        }

        return modifiedSentence;
    }




}