package com.gempukku.stccg.cardparsing;

import com.google.common.collect.Iterables;
import org.checkerframework.checker.regex.qual.Regex;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayCardActionParsing {

    public static void main(String[] args) {
        System.out.println("if you don't see this, the text got truncated");
        int matchingSentences = 0;
        int wordInstances = 0;

        Pattern playPattern = Pattern.compile("play", Pattern.CASE_INSENSITIVE);
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
                        throw new RuntimeException("Sentence matched " + matchingResults.size() + " subPatterns");
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

        // Game terms not related to card play actions
        result.add(new RegexDescription(Pattern.compile("player", Pattern.CASE_INSENSITIVE), "player"));
        result.add(new RegexDescription(Pattern.compile("[\\s(\"]in\\splay"), "in play"));
        result.add(new RegexDescription(Pattern.compile("from\\splay[^i]"), "from play"));
        result.add(new RegexDescription(Pattern.compile("out[-\\s]of[-\\s]play"), "out of play"));
        result.add(new RegexDescription(Pattern.compile("leave\\splay[^eis]"), "leave play"));
        result.add(new RegexDescription(Pattern.compile("leaves\\splay[^eis]"), "leaves play"));

        result.add(new RegexDescription(Pattern.compile("played", Pattern.CASE_INSENSITIVE), "played"));
        result.add(new RegexDescription(Pattern.compile("playing"), "playing"));

        // Play action as a required response to or cost for another action
        result.add(new RegexDescription(Pattern.compile("you may.+[,:] (then )?play(?=\\s)"), "you may..., then play"));
        result.add(new RegexDescription(Pattern.compile("you must play \\S+ to", Pattern.CASE_INSENSITIVE), "you must play... to"));
        result.add(new RegexDescription(Pattern.compile("must\\simmediately play[^eis]"), "must immediately play"));

        // Allowing cards to be played
        result.add(new RegexDescription(Pattern.compile("allowing.+to\\senter\\splay", Pattern.CASE_INSENSITIVE), "allowing... to enter play"));

        // Not allowing cards to be played

        result.add(new RegexDescription(Pattern.compile("[^o]\\senter\\splay[^eis]", Pattern.CASE_INSENSITIVE), "enter play"));
        result.add(new RegexDescription(Pattern.compile("(can|may)\\s(?!only)(not )?(immediately )?(.+ or )?(immediately )?play[^es]", Pattern.CASE_INSENSITIVE), "can/may (not/immediately) (? or) (immediately) play"));
        result.add(new RegexDescription(Pattern.compile("[^f]\\syou\\splay[^es]"), "you play"));
        result.add(new RegexDescription(Pattern.compile("card\\splay[^eis]"), "card play"));
        result.add(new RegexDescription(Pattern.compile("may only (seed or )?play"), "may only (seed or) play"));
        result.add(new RegexDescription(Pattern.compile("play at any time"), "play at any time"));
        result.add(new RegexDescription(Pattern.compile("and\\s(\\(once per turn\\) )?play[^eis]"), "and play"));
        result.add(new RegexDescription(Pattern.compile("suspend(s)?(ing)?\\splay[^eis]", Pattern.CASE_INSENSITIVE), "suspend(s/ing) play"));
        result.add(new RegexDescription(Pattern.compile("enter(ed|s)\\splay[^eis]", Pattern.CASE_INSENSITIVE), "entered/enters play"));
        result.add(new RegexDescription(Pattern.compile("[^ny]\\splay for free"), "play for free"));
        result.add(new RegexDescription(Pattern.compile("otherwise\\splay[^eis]"), "otherwise play"));
        result.add(new RegexDescription(Pattern.compile("[^-\\s]play[^es]"), "Lowercase with no leading space"));
        result.add(new RegexDescription(Pattern.compile("play on table", Pattern.CASE_INSENSITIVE), "play on table"));
        result.add(new RegexDescription(Pattern.compile("plays", Pattern.CASE_INSENSITIVE), "plays"));
        result.add(new RegexDescription(Pattern.compile("^play[^es][^o][^n]", Pattern.CASE_INSENSITIVE), "Start of sentence"));
        result.add(new RegexDescription(Pattern.compile("^(immediately )?play on [^t]", Pattern.CASE_INSENSITIVE), "play on"));
        result.add(new RegexDescription(Pattern.compile("^Play one"), "Play one at beginning of sentence"));
        result.add(new RegexDescription(Pattern.compile("If .+, play\\s"), "If..., play"));
        result.add(new RegexDescription(Pattern.compile(" to play\\s[^f]"), " to play"));
        result.add(new RegexDescription(Pattern.compile("if you.+, or play"), "if you..., or play"));
        result.add(new RegexDescription(Pattern.compile("OR play on any spaceline"), "OR play on any spaceline"));
        result.add(new RegexDescription(Pattern.compile("if you (subsequently )?play\\s", Pattern.CASE_INSENSITIVE), "if you play"));
        result.add(new RegexDescription(Pattern.compile("play or place in hand", Pattern.CASE_INSENSITIVE), "play or place in hand"));
        result.add(new RegexDescription(Pattern.compile("put it into play"), "put it into play"));
        result.add(new RegexDescription(Pattern.compile("play Wormhole"), "play Wormhole"));
        result.add(new RegexDescription(Pattern.compile("download .+[^t]\\sinto play"), "download into play"));
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