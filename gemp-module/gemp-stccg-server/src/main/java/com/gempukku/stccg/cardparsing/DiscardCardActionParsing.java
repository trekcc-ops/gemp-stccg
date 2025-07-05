package com.gempukku.stccg.cardparsing;

import com.google.common.collect.Iterables;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscardCardActionParsing {

    public static void main(String[] args) {

        List<RegexDescription> regexes = getRegexDescriptions();
        Map<RegexDescription, List<RegexResult>> resultMap = getResultMap(regexes);

        showAllRegexes(regexes, resultMap);
        // showResponseRegexes(regexes, resultMap);
    }

    private static Map<RegexDescription, List<RegexResult>> getResultMap(List<RegexDescription> regexes) {
        System.out.println("if you don't see this, the text got truncated");
        int matchingSentences = 0;
        int wordInstances = 0;

        Pattern playPattern = Pattern.compile("discard", Pattern.CASE_INSENSITIVE);
        List<RegexResult> regexResults = new LinkedList<>();
        Map<RegexDescription, List<RegexResult>> resultMap = new HashMap<>();
        for (RegexDescription descr : regexes) {
            resultMap.put(descr, new LinkedList<>());
        }

        Map<String, CardData> _newLibraryMap = LibraryFunctions.createLibrary();
        for (CardData card : _newLibraryMap.values()) {
            for (Sentence sentence : card._gameText.getSentences()) {
                String sentenceText = sentence.toString();

                Matcher parentMatcher = playPattern.matcher(sentenceText);
                boolean sentenceMatches = false;
                while (parentMatcher.find()) { // Iterate through matches on the parent pattern
                    sentenceMatches = true;
                    wordInstances++;
                    int start = parentMatcher.start();
                    int end = parentMatcher.end();
                    List<RegexResult> matchingResults = new LinkedList<>();
                    for (RegexDescription regexDescr : regexes) {
                        Matcher subMatcher = regexDescr.getRegex().matcher(sentenceText);
                        while (subMatcher.find()) {
                            int subStart = subMatcher.start();
                            int subEnd = subMatcher.end();
                            if (subMatcher.start() <= start && subMatcher.end() >= end) {
                                RegexResult subResult =
                                        new RegexResult(card._title, sentenceText, subStart, subEnd, regexDescr);
                                matchingResults.add(subResult);
                            }
                        }
                    }
                    if (matchingResults.size() != 1) {
                        System.out.println("[" + matchingResults.size() + "] " + card._title + " - " + sentence);
                        for (RegexResult result : matchingResults) {
                            System.out.println(result._regexDescription.getRegex());
                        }
                        //                     throw new RuntimeException("Sentence matched " + matchingResults.size() + " subPatterns");
                    } else {
                        RegexResult subMatchResult = Iterables.getOnlyElement(matchingResults);
                        regexResults.add(subMatchResult);
                        resultMap.get(subMatchResult._regexDescription).add(subMatchResult);
                    }
                }
                if (sentenceMatches) {
                    matchingSentences++;
                }
            }
        }
        System.out.println(wordInstances + " instances of word");
        System.out.println(regexResults.size() + " results matched");
        System.out.println(matchingSentences + " sentences");
        return resultMap;
    }

    private static void showAllRegexes(List<RegexDescription> regexes, Map<RegexDescription, List<RegexResult>> resultMap) {
        for (RegexDescription descr : regexes) {
            List<RegexResult> descResults = resultMap.get(descr);
            if (!descResults.isEmpty()) {
                System.out.println(descr._description + " - " + resultMap.get(descr).size());
                for (int i = 0; i < 30 && i < descResults.size(); i++) {
                    System.out.println("    " + descResults.get(i).getSentenceWithHighlights());
                }
            }
        }
    }

    private static void showResponseRegexes(List<RegexDescription> regexes, Map<RegexDescription, List<RegexResult>> resultMap) {
        for (RegexDescription descr : regexes) {
            List<RegexResult> descResults = resultMap.get(descr);
            if (!descResults.isEmpty() && descr._description.startsWith("RESPONSE")) {
                System.out.println(descr._description + " - " + resultMap.get(descr).size());
                for (RegexResult descResult : descResults) {
                    System.out.println("    " + descResult.getSentenceWithHighlights());
                }
            }
        }
    }

    private static List<RegexDescription> getRegexDescriptions() {
        List<RegexDescription> result = new LinkedList<>();

        String countCardRegex = "(a|an|one|one of your|2|two|up to three|3|three|up to five|your|any number of)";
        StringJoiner characteristics = new StringJoiner("|");
        characteristics.add("\\[AU\\] card");
        characteristics.add("card");
        characteristics.add("\\[Com\\] or \\[Def\\] drone");
        characteristics.add("counterpart");
        characteristics.add("Dissident");
        characteristics.add("\\[Doorway\\]");
        characteristics.add("\\[DQ\\] Klingons");
        characteristics.add("drone");
        characteristics.add("equipment");
        characteristics.add("Equipment card");
        characteristics.add("event");
        characteristics.add("Gold-Pressed Latinum");
        characteristics.add("Gold-Pressed Latinum card");
        characteristics.add("MEDICAL personnel");
        characteristics.add("(non-)?personnel card");
        characteristics.add("\\[OS\\] OFFICER");
        characteristics.add("\\[TNG\\] card");
        characteristics.add("unique non-\\[Bor\\] card");
        characteristics.add("\\[Univ\\]\\[Non\\] or \\[Non\\]\\[OS\\] personnel");
        characteristics.add("Youth");
        String allCharacteristics = characteristics.toString();

        String characteristicRegex = "(" + allCharacteristics + ")(s)?";

        String cardTitleRegex = "(Claw|Field|Latinum|Pod|Scow)";

        // Game terms not related to discard action
        result.add(new RegexDescription(Pattern.compile("discard pile", Pattern.CASE_INSENSITIVE), "discard pile"));

        // Responding to discard action
        result.add(new RegexDescription(Pattern.compile("discarded", Pattern.CASE_INSENSITIVE), "discarded"));
        result.add(new RegexDescription(Pattern.compile("if that player discards", Pattern.CASE_INSENSITIVE), "discards"));

        // Discard specific card
        result.add(new RegexDescription(Pattern.compile("discard probe card", Pattern.CASE_INSENSITIVE), "discard probe card"));

        // Discard this card (probably)
        result.add(new RegexDescription(Pattern.compile("discard (this )?artifact", Pattern.CASE_INSENSITIVE), "discard artifact"));
        result.add(new RegexDescription(Pattern.compile("discard (this )?dilemma", Pattern.CASE_INSENSITIVE), "discard dilemma"));
        result.add(new RegexDescription(Pattern.compile("discard (this )?doorway", Pattern.CASE_INSENSITIVE), "discard doorway"));
        result.add(new RegexDescription(Pattern.compile("discard (this )?event", Pattern.CASE_INSENSITIVE), "discard event"));
        result.add(new RegexDescription(Pattern.compile("discard (this )?incident", Pattern.CASE_INSENSITIVE), "discard incident"));
        result.add(new RegexDescription(Pattern.compile("discard (this )?interrupt", Pattern.CASE_INSENSITIVE), "discard interrupt"));
        result.add(new RegexDescription(Pattern.compile("discard (this )?objective", Pattern.CASE_INSENSITIVE), "discard objective"));
        result.add(new RegexDescription(Pattern.compile("Discard after"), "'discard after'"));
        result.add(new RegexDescription(Pattern.compile("Discard if"), "'discard if'"));
        result.add(new RegexDescription(Pattern.compile("(, d|D)iscard to"), "'discard to'"));
        result.add(new RegexDescription(Pattern.compile("discard this card"), "'discard this card'"));
        result.add(new RegexDescription(Pattern.compile("then discard\\."), "'then discard.'"));
/*        result.add(new RegexDescription(Pattern.compile("(then )?([;,] d|D)iscard [dD]ilemma\\."), "'Discard dilemma.'"));
        result.add(new RegexDescription(Pattern.compile("(then )?([;,] d|D)iscard [dD]oorway\\."), "'Discard doorway.'"));
        result.add(new RegexDescription(Pattern.compile("(then )?([;,] d|D)iscard [eE]vent\\."), "'Discard event.'"));
        result.add(new RegexDescription(Pattern.compile("(then )?([;,] d|D)iscard [iI]ncident\\."), "'Discard incident.'"));
        result.add(new RegexDescription(Pattern.compile("(then )?([;,] d|D)iscard [oO]bjective\\."), "'Discard objective.'")); */

        // Discard card present or here
        result.add(new RegexDescription(Pattern.compile("discard(ing)? "+countCardRegex+" "+characteristicRegex+" present", Pattern.CASE_INSENSITIVE), "discard card present"));
        result.add(new RegexDescription(Pattern.compile("discard(ing)? "+countCardRegex+" "+characteristicRegex+" here", Pattern.CASE_INSENSITIVE), "discard card here"));

        // Discard card from hand
        result.add(new RegexDescription(Pattern.compile("discard(ing)? "+countCardRegex+" "+characteristicRegex+" (from|in) hand", Pattern.CASE_INSENSITIVE), "discard card from hand"));
        result.add(new RegexDescription(Pattern.compile("discard (entire|your) hand", Pattern.CASE_INSENSITIVE), "discard entire hand"));
        result.add(new RegexDescription(Pattern.compile("discard from hand", Pattern.CASE_INSENSITIVE), "discard from hand"));

        // Discard cards (not specified if from hand or in play)
        result.add(new RegexDescription(Pattern.compile("discard(ing)? "+countCardRegex+" "+characteristicRegex+"(?!(s)? present)(?!(s)? (from|in) hand)(?!(s)? here)", Pattern.CASE_INSENSITIVE), "discard card [generic]"));

        // Discard card by name
        result.add(new RegexDescription(Pattern.compile("discard " + cardTitleRegex, Pattern.CASE_INSENSITIVE), "discard card by title"));

        // Maybe unclear?
        result.add(new RegexDescription(Pattern.compile("discard(ing)? (the other|top three|it |one of same type|that card|others|duplicates|both)", Pattern.CASE_INSENSITIVE), "antecedent?"));

        return result;
    }

    private static boolean hasNoOverlap(String text, Pattern parentPattern, List<RegexDescription> subPatterns) {
        int subMatches = 0;
        int parentMatches = parentPattern.matcher(text).groupCount();

        for (RegexDescription subPattern : subPatterns) {
            subMatches = subMatches + subPattern.getRegex().matcher(text).groupCount();
        }
        return parentMatches == subMatches;
    }

    private static List<RegexDescription> commonDescriptions() {
        List<RegexDescription> result = new LinkedList<>();
        result.add(new RegexDescription(Pattern.compile("\\splay"), "Lowercase with a space"));
        result.add(new RegexDescription(Pattern.compile("[^\\s]play"), "Lowercase with no space"));
        result.add(new RegexDescription(Pattern.compile("Play"), "Uppercase"));
        return result;
    }

}