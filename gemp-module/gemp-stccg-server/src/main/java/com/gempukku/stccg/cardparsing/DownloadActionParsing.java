package com.gempukku.stccg.cardparsing;

import com.google.common.collect.Iterables;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadActionParsing {

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

        Pattern downloadPattern = Pattern.compile("download", Pattern.CASE_INSENSITIVE);
        List<RegexResult> regexResults = new LinkedList<>();
        Map<RegexDescription, List<RegexResult>> resultMap = new HashMap<>();
        for (RegexDescription descr : regexes) {
            resultMap.put(descr, new LinkedList<>());
        }

        Map<String, CardData> _newLibraryMap = LibraryFunctions.createLibrary();
        for (CardData card : _newLibraryMap.values()) {
            for (Sentence sentence : card._gameText.getSentences()) {
                String sentenceText = sentence.toString();

                Matcher parentMatcher = downloadPattern.matcher(sentenceText);
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
                for (int i = 0; i < 50 && i < descResults.size(); i++) {
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

        // in place of your normal card download
        result.add(new RegexDescription(Pattern.compile("in place of your normal card download, you may download", Pattern.CASE_INSENSITIVE), "In place of your normal card download"));

        // once per turn/game
        result.add(new RegexDescription(Pattern.compile("once each turn, (you )?may (discard objective to )?download", Pattern.CASE_INSENSITIVE), "once per turn"));
        result.add(new RegexDescription(Pattern.compile("once per game[,)] download ", Pattern.CASE_INSENSITIVE), "once per game"));

        // Modify timing of card download actions
        result.add(new RegexDescription(Pattern.compile("cards download at any time"), "download at any time"));

        // download action as a required response to or cost for another action
        result.add(new RegexDescription(Pattern.compile("you may.+[,:] (then )?download(?=\\s)"), "you may..., then download"));
        result.add(new RegexDescription(Pattern.compile("you must download \\S+ to", Pattern.CASE_INSENSITIVE), "you must download... to"));
        result.add(new RegexDescription(Pattern.compile("must (if possible) download", Pattern.CASE_INSENSITIVE), "must (if possible) download"));
        result.add(new RegexDescription(Pattern.compile("must\\simmediately download[^eis]"), "must immediately download"));
        result.add(new RegexDescription(Pattern.compile("If .+, download\\s"), "If..., download"));
        result.add(new RegexDescription(Pattern.compile("put it into download"), "put it into download"));
            // Cochrane Memorial - "download one personnel to planet"
        result.add(new RegexDescription(Pattern.compile("\\sdownload one personnel"), "'download one personnel' at start of sentence"));
        result.add(new RegexDescription(Pattern.compile("must be downloaded", Pattern.CASE_INSENSITIVE), "'must be downloaded'"));
        result.add(new RegexDescription(Pattern.compile("Opponent downloads"), "'Opponent downloads' at start of sentence"));

        // Allowing cards to be downloaded (or not)
        result.add(new RegexDescription(Pattern.compile("allowing.+to\\senter\\sdownload", Pattern.CASE_INSENSITIVE), "allowing... to enter download"));
        result.add(new RegexDescription(Pattern.compile("can be downloaded", Pattern.CASE_INSENSITIVE), "'can be downloaded'"));
        result.add(new RegexDescription(Pattern.compile("(can|may)\\s(?!only)(not )?(immediately |then )?(.+ or )?(immediately )?(download[^es]|be downloaded)", Pattern.CASE_INSENSITIVE), "can/may (not/immediately) (? or) (immediately) download"));

        // Response to a card download action
        result.add(new RegexDescription(Pattern.compile("if you.+, or download"), "RESPONSE: if you..., or download"));
        result.add(new RegexDescription(Pattern.compile("if you (subsequently )?download\\s(\\()?(or have downloaded)?", Pattern.CASE_INSENSITIVE), "RESPONSE: if you download"));
        result.add(new RegexDescription(Pattern.compile("if you have downloaded", Pattern.CASE_INSENSITIVE), "RESPONSE: if you have downloaded"));
        result.add(new RegexDescription(Pattern.compile("when\\syou\\sdownload[^es]", Pattern.CASE_INSENSITIVE), "RESPONSE: when you download"));
        result.add(new RegexDescription(Pattern.compile("time\\syou\\sdownload[^es]", Pattern.CASE_INSENSITIVE), "RESPONSE: time you download"));
        result.add(new RegexDescription(Pattern.compile("just[-\\s]downloaded", Pattern.CASE_INSENSITIVE), "RESPONSE: 'just downloaded'"));
        result.add(new RegexDescription(Pattern.compile("if (they )?downloaded", Pattern.CASE_INSENSITIVE), "RESPONSE: 'if [they] downloaded'"));
        result.add(new RegexDescription(Pattern.compile("your personnel enter download stopped", Pattern.CASE_INSENSITIVE), "RESPONSE: 'your personnel enter download stopped'"));
        result.add(new RegexDescription(Pattern.compile("enter(ed|s)\\sdownload[^eis]", Pattern.CASE_INSENSITIVE), "RESPONSE: entered/enters download"));
        result.add(new RegexDescription(Pattern.compile("opponent\\sdownloads"), "RESPONSE: 'opponent downloads'"));
        result.add(new RegexDescription(Pattern.compile("[^,rt]\\sdownloads"), "RESPONSE: 'downloads' following space"));
        result.add(new RegexDescription(Pattern.compile("Q's Tent downloaded from hand"), "RESPONSE: 'Q's Tent downloaded from hand'"));

        // Counting from gamestate or action history
        result.add(new RegexDescription(Pattern.compile("each of your objectives downloaded"), "each of your objectives downloaded"));

        // Description of how this or another card is downloaded
        result.add(new RegexDescription(Pattern.compile("\\(downloads for free if"), "downloads for free if"));
        result.add(new RegexDescription(Pattern.compile("\\sDownloads"), "'downloads' at beginning of sentence following space"));
        result.add(new RegexDescription(Pattern.compile("^Downloads"), "'downloads' at beginning of sentence"));
        result.add(new RegexDescription(Pattern.compile(",\\sdownloads"), ", downloads'"));
        result.add(new RegexDescription(Pattern.compile("Seeds\\sor\\sdownloads"), "Seeds or downloads"));
        result.add(new RegexDescription(Pattern.compile(",\\sseeds\\sor\\sdownloads"), ", seeds or downloads"));
        result.add(new RegexDescription(Pattern.compile("[^s]\\sor\\sdownloads"), "'or downloads'"));
        result.add(new RegexDescription(Pattern.compile("^download one"), "'download one' at beginning of sentence"));
        result.add(new RegexDescription(Pattern.compile("place in hand until downloaded", Pattern.CASE_INSENSITIVE), "'place in hand until downloaded'"));
        result.add(new RegexDescription(Pattern.compile("download on table", Pattern.CASE_INSENSITIVE), "download on table"));
        result.add(new RegexDescription(Pattern.compile("^download[^es][^o][^n]", Pattern.CASE_INSENSITIVE), "Start of sentence"));
        result.add(new RegexDescription(Pattern.compile("; download ", Pattern.CASE_INSENSITIVE), "'download' after a semicolon"));

        // Restricting card download actions
        result.add(new RegexDescription(Pattern.compile("may\\snot\\sotherwise\\sdownload[^eis]"), "may not otherwise download"));
        result.add(new RegexDescription(Pattern.compile("may not be downloaded", Pattern.CASE_INSENSITIVE), "'may not be downloaded'"));
        result.add(new RegexDescription(Pattern.compile("may not enter download", Pattern.CASE_INSENSITIVE), "'may not enter download'"));
        result.add(new RegexDescription(Pattern.compile("[^efn]\\syou\\sdownload must be"), "'you download must be'"));
        result.add(new RegexDescription(Pattern.compile("may only (seed or )?download"), "may only (seed or) download"));
        result.add(new RegexDescription(Pattern.compile("may only download .+[^t]\\sinto download"), "may only download into download"));

        // Unclear
        result.add(new RegexDescription(Pattern.compile("to be downloaded", Pattern.CASE_INSENSITIVE), "'to be downloaded'"));
        result.add(new RegexDescription(Pattern.compile("downloading"), "downloading"));
        result.add(new RegexDescription(Pattern.compile("card\\sdownload[^eis]"), "card download"));
        result.add(new RegexDescription(Pattern.compile("and\\s(\\(once per turn\\) )?download[^eis]"), "and download"));
        result.add(new RegexDescription(Pattern.compile("suspend(s)?(ing)?\\sdownload[^eis]", Pattern.CASE_INSENSITIVE), "suspend(s/ing) download"));
        result.add(new RegexDescription(Pattern.compile("[^ny]\\sdownload for free"), "download for free"));
        result.add(new RegexDescription(Pattern.compile("^(immediately )?download on [^t]", Pattern.CASE_INSENSITIVE), "download on"));
        result.add(new RegexDescription(Pattern.compile(" to download\\s[^f]"), " to download"));
        result.add(new RegexDescription(Pattern.compile("OR download on any spaceline"), "OR download on any spaceline"));
        result.add(new RegexDescription(Pattern.compile("download or place in hand", Pattern.CASE_INSENSITIVE), "download or place in hand"));
        result.add(new RegexDescription(Pattern.compile("download Wormhole"), "download Wormhole"));
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
        result.add(new RegexDescription(Pattern.compile("\\sdownload"), "Lowercase with a space"));
        result.add(new RegexDescription(Pattern.compile("[^\\s]download"), "Lowercase with no space"));
        result.add(new RegexDescription(Pattern.compile("download"), "Uppercase"));
        return result;
    }

}