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

        Pattern reportPattern = Pattern.compile("report", Pattern.CASE_INSENSITIVE);
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

                Matcher parentMatcher = reportPattern.matcher(sentenceText);
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

        // Game terms not related to card report actions
        result.add(new RegexDescription(Pattern.compile("reporter", Pattern.CASE_INSENSITIVE), "reporter"));
        result.add(new RegexDescription(Pattern.compile("[\\s(\"]in\\sreport"), "in report"));
        result.add(new RegexDescription(Pattern.compile("from\\sreport[^i]"), "from report"));
        result.add(new RegexDescription(Pattern.compile("out[-\\s]of[-\\s]report"), "out of report"));
        result.add(new RegexDescription(Pattern.compile("leave\\sreport[^eis]"), "leave report"));
        result.add(new RegexDescription(Pattern.compile("leaves\\sreport[^eis]"), "leaves report"));
        result.add(new RegexDescription(Pattern.compile("File Mission report"), "File Mission report"));

        result.add(new RegexDescription(Pattern.compile("reported", Pattern.CASE_INSENSITIVE), "reported"));
        result.add(new RegexDescription(Pattern.compile("reporting"), "reporting"));

        // report action as a required response to or cost for another action
        result.add(new RegexDescription(Pattern.compile("you may.+[,:] (then )?report(?=\\s)"), "you may..., then report"));
        result.add(new RegexDescription(Pattern.compile("you must report \\S+ to", Pattern.CASE_INSENSITIVE), "you must report... to"));
        result.add(new RegexDescription(Pattern.compile("must\\simmediately report[^eis]"), "must immediately report"));

        // Allowing cards to be reported
        result.add(new RegexDescription(Pattern.compile("allowing.+to\\senter\\sreport", Pattern.CASE_INSENSITIVE), "allowing... to enter report"));

        // Not allowing cards to be reported

        result.add(new RegexDescription(Pattern.compile("[^o]\\senter\\sreport[^eis]", Pattern.CASE_INSENSITIVE), "enter report"));
        result.add(new RegexDescription(Pattern.compile("(can|may)\\s(?!only)(not )?(immediately )?(.+ or )?(immediately )?report[^es]", Pattern.CASE_INSENSITIVE), "can/may (not/immediately) (? or) (immediately) report"));
        result.add(new RegexDescription(Pattern.compile("[^f]\\syou\\sreport[^es]"), "you report"));
        result.add(new RegexDescription(Pattern.compile("card\\sreport[^eis]"), "card report"));
        result.add(new RegexDescription(Pattern.compile("may only (seed or )?report"), "may only (seed or) report"));
        result.add(new RegexDescription(Pattern.compile("report at any time"), "report at any time"));
        result.add(new RegexDescription(Pattern.compile("and\\s(\\(once per turn\\) )?report[^eis]"), "and report"));
        result.add(new RegexDescription(Pattern.compile("suspend(s)?(ing)?\\sreport[^eis]", Pattern.CASE_INSENSITIVE), "suspend(s/ing) report"));
        result.add(new RegexDescription(Pattern.compile("enter(ed|s)\\sreport[^eis]", Pattern.CASE_INSENSITIVE), "entered/enters report"));
        result.add(new RegexDescription(Pattern.compile("[^ny]\\sreport for free"), "report for free"));
        result.add(new RegexDescription(Pattern.compile("otherwise\\sreport[^eis]"), "otherwise report"));
        result.add(new RegexDescription(Pattern.compile("[^-\\s]report[^es]"), "Lowercase with no leading space"));
        result.add(new RegexDescription(Pattern.compile("report on table", Pattern.CASE_INSENSITIVE), "report on table"));
        result.add(new RegexDescription(Pattern.compile("reports", Pattern.CASE_INSENSITIVE), "reports"));
        result.add(new RegexDescription(Pattern.compile("^report[^es][^o][^n]", Pattern.CASE_INSENSITIVE), "Start of sentence"));
        result.add(new RegexDescription(Pattern.compile("^(immediately )?report on [^t]", Pattern.CASE_INSENSITIVE), "report on"));
        result.add(new RegexDescription(Pattern.compile("^report one"), "report one at beginning of sentence"));
        result.add(new RegexDescription(Pattern.compile("If .+, report\\s"), "If..., report"));
        result.add(new RegexDescription(Pattern.compile(" to report\\s[^f]"), " to report"));
        result.add(new RegexDescription(Pattern.compile("if you.+, or report"), "if you..., or report"));
        result.add(new RegexDescription(Pattern.compile("OR report on any spaceline"), "OR report on any spaceline"));
        result.add(new RegexDescription(Pattern.compile("if you (subsequently )?report\\s", Pattern.CASE_INSENSITIVE), "if you report"));
        result.add(new RegexDescription(Pattern.compile("report or place in hand", Pattern.CASE_INSENSITIVE), "report or place in hand"));
        result.add(new RegexDescription(Pattern.compile("put it into report"), "put it into report"));
        result.add(new RegexDescription(Pattern.compile("report Wormhole"), "report Wormhole"));
        result.add(new RegexDescription(Pattern.compile("download .+[^t]\\sinto report"), "download into report"));
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
        result.add(new RegexDescription(Pattern.compile("\\sreport"), "Lowercase with a space"));
        result.add(new RegexDescription(Pattern.compile("[^\\s]report"), "Lowercase with no space"));
        result.add(new RegexDescription(Pattern.compile("report"), "Uppercase"));
        return result;
    }

}