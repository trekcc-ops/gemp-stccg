package com.gempukku.stccg.cardparsing;

import com.google.common.collect.Iterables;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReportCardActionParsing {

    public static void main(String[] args) {
        System.out.println("if you don't see this, the text got truncated");
        int matchingSentences = 0;
        int wordInstances = 0;

        Pattern assimilatePattern = Pattern.compile("assimilate", Pattern.CASE_INSENSITIVE);
        List<RegexDescription> regexes = getRegexDescriptions();
        List<RegexResult> regexResults = new LinkedList<>();
        Map<RegexDescription, List<RegexResult>> resultMap = new HashMap<>();
        for (RegexDescription descr : regexes) {
            resultMap.put(descr, new LinkedList<>());
        }

        Map<String, CardData> _newLibraryMap = LibraryFunctions.createLibrary();
        for (CardData card : _newLibraryMap.values()) {
            for (Sentence sentence : card._gameText.getSentences()) {
                String sentenceText = sentence.toString();

                Matcher parentMatcher = assimilatePattern.matcher(sentenceText);
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
//                        throw new RuntimeException("Sentence matched " + matchingResults.size() + " subPatterns");
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

        for (RegexDescription descr : regexes) {
            List<RegexResult> descResults = resultMap.get(descr);
            if (!descResults.isEmpty()) {
                System.out.println(descr._description + " - " + resultMap.get(descr).size());
                for (int i = 0; i < 5 && i < descResults.size(); i++) {
                    System.out.println("    " + descResults.get(i).getSentenceWithHighlights());
                }
            }
        }
    }

    private static List<RegexDescription> getRegexDescriptions() {
        List<RegexDescription> result = new LinkedList<>();

        // Game terms not related to card assimilate actions
        result.add(new RegexDescription(Pattern.compile("assimilateer", Pattern.CASE_INSENSITIVE), "assimilateer"));
        result.add(new RegexDescription(Pattern.compile("[\\s(\"]in\\sassimilate"), "in assimilate"));
        result.add(new RegexDescription(Pattern.compile("from\\sassimilate[^i]"), "from assimilate"));
        result.add(new RegexDescription(Pattern.compile("out[-\\s]of[-\\s]assimilate"), "out of assimilate"));
        result.add(new RegexDescription(Pattern.compile("leave\\sassimilate[^eis]"), "leave assimilate"));
        result.add(new RegexDescription(Pattern.compile("leaves\\sassimilate[^eis]"), "leaves assimilate"));
        result.add(new RegexDescription(Pattern.compile("File Mission assimilate"), "File Mission assimilate"));

        result.add(new RegexDescription(Pattern.compile("assimilateed", Pattern.CASE_INSENSITIVE), "assimilateed"));
        result.add(new RegexDescription(Pattern.compile("assimilateing"), "assimilateing"));

        // assimilate action as a required response to or cost for another action
        result.add(new RegexDescription(Pattern.compile("you may.+[,:] (then )?assimilate(?=\\s)"), "you may..., then assimilate"));
        result.add(new RegexDescription(Pattern.compile("you must assimilate \\S+ to", Pattern.CASE_INSENSITIVE), "you must assimilate... to"));
        result.add(new RegexDescription(Pattern.compile("must\\simmediately assimilate[^eis]"), "must immediately assimilate"));

        // Allowing cards to be assimilateed
        result.add(new RegexDescription(Pattern.compile("allowing.+to\\senter\\sassimilate", Pattern.CASE_INSENSITIVE), "allowing... to enter assimilate"));

        // Not allowing cards to be assimilateed

        result.add(new RegexDescription(Pattern.compile("[^o]\\senter\\sassimilate[^eis]", Pattern.CASE_INSENSITIVE), "enter assimilate"));
        result.add(new RegexDescription(Pattern.compile("(can|may)\\s(?!only)(not )?(immediately )?(.+ or )?(immediately )?assimilate[^es]", Pattern.CASE_INSENSITIVE), "can/may (not/immediately) (? or) (immediately) assimilate"));
        result.add(new RegexDescription(Pattern.compile("[^f]\\syou\\sassimilate[^es]"), "you assimilate"));
        result.add(new RegexDescription(Pattern.compile("card\\sassimilate[^eis]"), "card assimilate"));
        result.add(new RegexDescription(Pattern.compile("may only (seed or )?assimilate"), "may only (seed or) assimilate"));
        result.add(new RegexDescription(Pattern.compile("assimilate at any time"), "assimilate at any time"));
        result.add(new RegexDescription(Pattern.compile("and\\s(\\(once per turn\\) )?assimilate[^eis]"), "and assimilate"));
        result.add(new RegexDescription(Pattern.compile("suspend(s)?(ing)?\\sassimilate[^eis]", Pattern.CASE_INSENSITIVE), "suspend(s/ing) assimilate"));
        result.add(new RegexDescription(Pattern.compile("enter(ed|s)\\sassimilate[^eis]", Pattern.CASE_INSENSITIVE), "entered/enters assimilate"));
        result.add(new RegexDescription(Pattern.compile("[^ny]\\sassimilate for free"), "assimilate for free"));
        result.add(new RegexDescription(Pattern.compile("otherwise\\sassimilate[^eis]"), "otherwise assimilate"));
        result.add(new RegexDescription(Pattern.compile("[^-\\s]assimilate[^es]"), "Lowercase with no leading space"));
        result.add(new RegexDescription(Pattern.compile("assimilate on table", Pattern.CASE_INSENSITIVE), "assimilate on table"));
        result.add(new RegexDescription(Pattern.compile("assimilates", Pattern.CASE_INSENSITIVE), "assimilates"));
        result.add(new RegexDescription(Pattern.compile("^assimilate[^es][^o][^n]", Pattern.CASE_INSENSITIVE), "Start of sentence"));
        result.add(new RegexDescription(Pattern.compile("^(immediately )?assimilate on [^t]", Pattern.CASE_INSENSITIVE), "assimilate on"));
        result.add(new RegexDescription(Pattern.compile("^assimilate one"), "assimilate one at beginning of sentence"));
        result.add(new RegexDescription(Pattern.compile("If .+, assimilate\\s"), "If..., assimilate"));
        result.add(new RegexDescription(Pattern.compile(" to assimilate\\s[^f]"), " to assimilate"));
        result.add(new RegexDescription(Pattern.compile("if you.+, or assimilate"), "if you..., or assimilate"));
        result.add(new RegexDescription(Pattern.compile("OR assimilate on any spaceline"), "OR assimilate on any spaceline"));
        result.add(new RegexDescription(Pattern.compile("if you (subsequently )?assimilate\\s", Pattern.CASE_INSENSITIVE), "if you assimilate"));
        result.add(new RegexDescription(Pattern.compile("assimilate or place in hand", Pattern.CASE_INSENSITIVE), "assimilate or place in hand"));
        result.add(new RegexDescription(Pattern.compile("put it into assimilate"), "put it into assimilate"));
        result.add(new RegexDescription(Pattern.compile("assimilate Wormhole"), "assimilate Wormhole"));
        result.add(new RegexDescription(Pattern.compile("download .+[^t]\\sinto assimilate"), "download into assimilate"));
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
        result.add(new RegexDescription(Pattern.compile("\\sassimilate"), "Lowercase with a space"));
        result.add(new RegexDescription(Pattern.compile("[^\\s]assimilate"), "Lowercase with no space"));
        result.add(new RegexDescription(Pattern.compile("assimilate"), "Uppercase"));
        return result;
    }

}