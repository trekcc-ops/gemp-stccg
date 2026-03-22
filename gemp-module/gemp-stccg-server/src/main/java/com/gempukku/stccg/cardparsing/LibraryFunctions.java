package com.gempukku.stccg.cardparsing;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.File;
import java.util.*;

@SuppressWarnings("all")
public class LibraryFunctions {
    public static Map<String, CardData> createLibrary() {
        Map<String, CardData> _newLibraryMap = new HashMap<>();
        List<CardData> _newLibrary = new LinkedList<>();
        File input;
        MappingIterator<Map<?, ?>> mappingIterator;
        List<Map<?, ?>> list;
        input = new File(".\\gemp-stccg\\gemp-module\\gemp-stccg-server\\src\\main\\resources\\Physical.csv");
        try {
            CsvSchema csv = CsvSchema.emptySchema().withHeader();
            CsvMapper csvMapper = new CsvMapper();
            mappingIterator =  csvMapper.reader().forType(Map.class).with(csv).readValues(input);
            list = mappingIterator.readAll();
            for (Map<?, ?> card : list) {
                CardData cardInfo = new CardData(card, "Physical");
                if (!cardInfo._formats.contains("ban_2E")) {
                    String cardTitle = cardInfo._title.replace(" *VP","");
                    if (_newLibraryMap.get(cardTitle) == null) {
                        _newLibraryMap.put(cardTitle, cardInfo);
                    } else {
                        if (!Objects.equals(_newLibraryMap.get(cardTitle)._rawGameText, cardInfo._rawGameText)) {
                            String newMapName = cardTitle + " (" + cardInfo._set + ")";
                            if (_newLibraryMap.get(newMapName) != null)
                                throw new RuntimeException("Shouldn't have happened");
                            else _newLibraryMap.put(newMapName, cardInfo);
                        }
                    }
                    _newLibrary.add(cardInfo);
                }
            }
            input = new File(".\\gemp-stccg\\gemp-module\\gemp-stccg-server\\src\\main\\resources\\Virtual.csv");
            mappingIterator =  csvMapper.reader().forType(Map.class).with(csv).readValues(input);
            list = mappingIterator.readAll();
            for (Map<?, ?> card : list) {
                CardData cardInfo = new CardData(card, "Virtual");
                if (!cardInfo._formats.contains("ban_2E")) {
                    String cardTitle = cardInfo._title.replace(" *VP","");
                    if (_newLibraryMap.get(cardTitle) == null) {
                        _newLibraryMap.put(cardTitle, cardInfo);
                    } else {
                        if (!Objects.equals(_newLibraryMap.get(cardTitle)._rawGameText, cardInfo._rawGameText)) {
                            String newMapName = cardTitle + " (" + cardInfo._set + ")";
                            if (_newLibraryMap.get(newMapName) != null)
                                throw new RuntimeException("Shouldn't have happened");
                            else _newLibraryMap.put(newMapName, cardInfo);
                        }
                    }
                    _newLibrary.add(cardInfo);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return _newLibraryMap;
    }

    public static List<String> splitIntoSentences(String text) {
        String periodFollowedBySpace = "(?<=\\.\\s)";
//        String periodFollowedBySpace = "[(?<=\\.\\s)|(?<=\\.\"\\s)]"; // also period before a quotation mark
        String notUSS = "(?<!U\\.S\\.S\\.\\s)";
        String notIKC = "(?<!I\\.K\\.C\\.\\s)";
        String notIKS = "(?<!I\\.K\\.S\\.\\s)";
        String notFollowedByLowercase = "(?![a-z])";
        String notFollowedByOrAndLowercase = "(?!OR\\s[a-z])";
        String notDr = "(?<!Dr\\.\\s)";
        String notvs = "(?<!\svs\\.\\s)";
        String notJamesT = "(?<!\sJames\\sT\\.\\s)";
        String fullRegex =
                periodFollowedBySpace + notUSS + notIKC + notIKS + notFollowedByLowercase +
                        notFollowedByOrAndLowercase + notDr + notvs + notJamesT;
        String[] splitString = text.split(fullRegex);
        List<String> result = new ArrayList<>();
        for (String string : splitString) {
            result.add(string.strip());
        }
        return result;
    }

    public static boolean isSentenceUnderstood(String sentence) {
        String fullSentence = sentence;
        if (fullSentence.endsWith(".")) {
            fullSentence = fullSentence.substring(0, fullSentence.length() - 1);
        }

        // Places where you can play or seed a card. May require selection, but no antecedent or response required.
        String [] validPlayOnTargets = {
                "table",
                "your unattempted mission",
                "your space facility",
                "your Q's Tent",
                "your ship",
                "any Bajoran mission",
                "any Cardassian mission",
                "any Federation mission",
                "any Klingon mission",
                "an opponent's [P] mission",
                "a planet mission",
                "a mission",
                "a homeworld",
                "Quark's Bar",
                "Ferengi Trading Post",
                "a personnel you own who is a captive OR under opponent's control",
                "your Acquisition personnel",
                "your Nagus",
                "Ops",
                "one personnel you've captured",
                "any Klingon who survived a losing battle",
                "any one Klingon",
                "any non-aligned ship",
                "a mission in a region",
                "your Acquisition personnel in their native quadrant",
                "any one personnel",
                "your Borg, android or any Geordi",
                "one of your personnel",
                "John Doe after he has prevented a death",
                "your [Fer] ship",
                "a mission with \"array\" or \"listening post\" in lore",
                "your [MQ] ship",
                "a non-Borg personnel who has INTEGRITY<8 and no Honor",
                "a Cardassia Region [P]",
                "a [S] location",
                "your [Vul] personnel",
                "any Vulcan mission",
                "any [VUL] mission",
                "any Non-Aligned mission (or any mission with \"Andorian\" in lore)",
                "your personnel with Navigation x2 or Stellar Cartography x2",
                "your Borg",
                "any ship",
                "a Neutral Zone Region mission",
                "a time location",
                "your [Orb] personnel",
                "your Mindmeld personnel",
                "your undamaged ship with a Cloaking Device",
                "your Reman",
                "an Ore Processing Unit",
                "your non-[Fed] ship",
                "your infiltrator",
                "Calder II",
                "Veytan",
                "your Klingon"
        };

        List<String> fullSentences = new ArrayList<>();
        fullSentences.add("(Captain's Order.)");
        fullSentences.add("(Unique.)");
        fullSentences.add("(Cumulative.)");
        fullSentences.add("Discard objective after use");

        List<String> playOnSentenceBeginnings = new ArrayList<>();
        playOnSentenceBeginnings.add("Plays on");
        playOnSentenceBeginnings.add("Seeds or plays on");
        playOnSentenceBeginnings.add("Seeds on");
        playOnSentenceBeginnings.add("Seed one on");

        for (String string : playOnSentenceBeginnings) {
            if (fullSentence.startsWith(string)) {
                String[] sentenceParts =
                        fullSentence.split("(?<=" + string.replace(" ","\\s") + ")");
                String target = sentenceParts[1].strip();
                if (sentenceParts.length == 2 && Arrays.stream(validPlayOnTargets).toList().contains(target))
                    return true;
                String[] targetParts = target.split("(\\sor\\s)");
                if (areValidTargets(targetParts, validPlayOnTargets))
                    return true;
            }
        }

        return fullSentences.contains(fullSentence);
    }

    public static boolean areValidTargets(String[] strings, String[] validTargets) {
        boolean result = true;
        for (String string : strings) {
            if (!Arrays.stream(validTargets).toList().contains(string))
                result = false;
        }
        return result;
    }
}