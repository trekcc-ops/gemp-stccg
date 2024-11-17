package com.gempukku.stccg.parsing;

import java.io.IOException;
import java.util.*;

public class StripSentencesTest extends NewLibraryTest {

    public void sentenceTest() throws IOException {
        createLibrary();
        int canBeParsed = 0;
        int cannotBeParsed = 0;

        List<String> includedTypes = new LinkedList<>();
        includedTypes.add("Event");
        includedTypes.add("Incident");
        includedTypes.add("Interrupt");

        for (CardData card : _newLibraryMap.values()) {
            if (includedTypes.contains(card._type) && Objects.equals(card._set, "TNG")) {
                for (Sentence sentence : card._gameText.getSentences()) {
                    String strippedSentence = stripSentence(sentence.toString());
                    if (strippedSentence.isEmpty()) {
                        canBeParsed++;
                    } else {
                        System.out.println(strippedSentence);
                        cannotBeParsed++;
                    }
                }
            }
        }
        System.out.println("Can be parsed: " + canBeParsed);
        System.out.println("Cannot be parsed: " + cannotBeParsed);
    }

    private String stripSentence(String originalSentence) {
        String modifiedSentence = originalSentence.toLowerCase();
        modifiedSentence = stripUsageLimitPhrases(modifiedSentence);
        modifiedSentence = stripIcons(modifiedSentence);
        modifiedSentence = stripLocations(modifiedSentence);
        modifiedSentence = stripVerbs(modifiedSentence);

        String[] sentencePieces = modifiedSentence.split("\\s+");
        String[] dumbWords = {"on", "or", "and", "."};
        boolean result = true;
        for (String string : sentencePieces) {
            String strippedString = string.strip();
            if (!Arrays.stream(dumbWords).toList().contains(strippedString))
                result = false;
        }

        if (result == true)
            return "";
        else
            return modifiedSentence;
    }

    private String stripUsageLimitPhrases(String originalSentence) {
        String modifiedSentence = originalSentence;
        List<String> usageLimits = new ArrayList();
        usageLimits.add("once per game");
        usageLimits.add("once each turn");
        usageLimits.add("once every turn");
        usageLimits.add("in place of your normal card play");

        for (String string : usageLimits) {
            String withComma = string + ",";
            modifiedSentence = modifiedSentence.replace(withComma, "").strip();
            modifiedSentence = modifiedSentence.replace(string, "").strip();
        }

        return modifiedSentence;
    }

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