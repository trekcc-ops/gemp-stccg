package com.gempukku.stccg.cardparsing;

import com.google.common.collect.Iterables;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KillActionParsing {

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

        Pattern killPattern = Pattern.compile("kill", Pattern.CASE_INSENSITIVE);
        List<RegexResult> regexResults = new LinkedList<>();
        Map<RegexDescription, List<RegexResult>> resultMap = new HashMap<>();
        for (RegexDescription descr : regexes) {
            resultMap.put(descr, new LinkedList<>());
        }

        Map<String, CardData> _newLibraryMap = LibraryFunctions.createLibrary();
        for (CardData card : _newLibraryMap.values()) {
            for (Sentence sentence : card._gameText.getSentences()) {
                String sentenceText = sentence.toString();

                Matcher parentMatcher = killPattern.matcher(sentenceText);
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
                for (int i = 0; i < 10 && i < descResults.size(); i++) {
                    System.out.println("    " + descResults.get(i).getCardTitle() + " - " + descResults.get(i).getSentenceWithHighlights());
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

        // Game terms not related to card kill actions
        result.add(new RegexDescription(Pattern.compile("skill", Pattern.CASE_INSENSITIVE), "skill"));
        result.add(new RegexDescription(Pattern.compile("Planet Killer"), "Planet Killer"));

        // Modify timing of card kill actions
        result.add(new RegexDescription(Pattern.compile("cards kill at any time"), "kill at any time"));

        // Select personnel to be killed
        result.add(new RegexDescription(Pattern.compile("\\(random selection(, round down)?\\) (are|is) killed", Pattern.CASE_INSENSITIVE), "(random selection) is killed"));
        result.add(new RegexDescription(Pattern.compile("kill(ed)?( one personnel)? \\(random selection\\)", Pattern.CASE_INSENSITIVE), "killed (random selection)"));
        result.add(new RegexDescription(Pattern.compile("killed, random selection"), "killed, random selection"));
        result.add(new RegexDescription(Pattern.compile("chooses one personnel to be killed", Pattern.CASE_INSENSITIVE), "chooses one personnel to be killed"));
//        result.add(new RegexDescription(Pattern.compile("randomly kill", Pattern.CASE_INSENSITIVE), "randomly kill"));


        // kill action as a required response to or cost for another action
        result.add(new RegexDescription(Pattern.compile("(once per game|once each turn), kill", Pattern.CASE_INSENSITIVE), "once per game/once each turn, kill"));
        result.add(new RegexDescription(Pattern.compile("you may.+[,:] (then )?kill(?=\\s)"), "you may..., then kill"));
        result.add(new RegexDescription(Pattern.compile("you must kill \\S+ to", Pattern.CASE_INSENSITIVE), "you must kill... to"));
        result.add(new RegexDescription(Pattern.compile("must\\simmediately kill[^eis]"), "must immediately kill"));
        result.add(new RegexDescription(Pattern.compile("If .+, kill\\s"), "If..., kill"));
        result.add(new RegexDescription(Pattern.compile("put it into kill"), "put it into kill"));
        result.add(new RegexDescription(Pattern.compile("must be killed", Pattern.CASE_INSENSITIVE), "'must be killed'"));
        result.add(new RegexDescription(Pattern.compile("Opponent kills"), "'Opponent kills' at start of sentence"));

        // Allowing cards to be killed
        result.add(new RegexDescription(Pattern.compile("allowing.+to\\senter\\skill", Pattern.CASE_INSENSITIVE), "allowing... to enter kill"));
        result.add(new RegexDescription(Pattern.compile("can be killed", Pattern.CASE_INSENSITIVE), "'can be killed'"));
        result.add(new RegexDescription(Pattern.compile("may be killed", Pattern.CASE_INSENSITIVE), "'may be killed'"));

        // Response to a card kill action
        result.add(new RegexDescription(Pattern.compile("(about|selected) to be killed", Pattern.CASE_INSENSITIVE), "about/selected to be killed"));
//        result.add(new RegexDescription(Pattern.compile("[^es] killed"), "killed"));
        result.add(new RegexDescription(Pattern.compile("if you.+, or kill"), "RESPONSE: if you..., or kill"));
        result.add(new RegexDescription(Pattern.compile("if you (subsequently )?kill\\s(\\()?(or have killed)?", Pattern.CASE_INSENSITIVE), "RESPONSE: if you kill"));
        result.add(new RegexDescription(Pattern.compile("if you have killed", Pattern.CASE_INSENSITIVE), "RESPONSE: if you have killed"));
        result.add(new RegexDescription(Pattern.compile("when\\syou\\skill[^es]", Pattern.CASE_INSENSITIVE), "RESPONSE: when you kill"));
        result.add(new RegexDescription(Pattern.compile("time\\syou\\skill[^es]", Pattern.CASE_INSENSITIVE), "RESPONSE: time you kill"));
        result.add(new RegexDescription(Pattern.compile("just[-\\s]killed", Pattern.CASE_INSENSITIVE), "RESPONSE: 'just killed'"));
        result.add(new RegexDescription(Pattern.compile("if (they )?killed", Pattern.CASE_INSENSITIVE), "RESPONSE: 'if [they] killed'"));
        result.add(new RegexDescription(Pattern.compile("your personnel enter kill stopped", Pattern.CASE_INSENSITIVE), "RESPONSE: 'your personnel enter kill stopped'"));
        result.add(new RegexDescription(Pattern.compile("enter(ed|s)\\skill[^eis]", Pattern.CASE_INSENSITIVE), "RESPONSE: entered/enters kill"));
        result.add(new RegexDescription(Pattern.compile("opponent\\skills"), "RESPONSE: 'opponent kills'"));
        result.add(new RegexDescription(Pattern.compile("Q's Tent killed from hand"), "RESPONSE: 'Q's Tent killed from hand'"));

        // Counting from gamestate or action history
        result.add(new RegexDescription(Pattern.compile("each of your objectives killed"), "each of your objectives killed"));

        // Description of how this or another card is killed
        result.add(new RegexDescription(Pattern.compile("\\(kills for free if"), "kills for free if"));
        result.add(new RegexDescription(Pattern.compile("\\skills"), "'kills' at beginning of sentence following space"));
        result.add(new RegexDescription(Pattern.compile("^kills"), "'kills' at beginning of sentence"));
        result.add(new RegexDescription(Pattern.compile("Seeds\\sor\\skills"), "Seeds or kills"));
        result.add(new RegexDescription(Pattern.compile(",\\sseeds\\sor\\skills"), ", seeds or kills"));
        result.add(new RegexDescription(Pattern.compile("[^s]\\sor\\skills"), "'or kills'"));
        result.add(new RegexDescription(Pattern.compile("^kill one"), "'kill one' at beginning of sentence"));
        result.add(new RegexDescription(Pattern.compile("place in hand until killed", Pattern.CASE_INSENSITIVE), "'place in hand until killed'"));
        result.add(new RegexDescription(Pattern.compile("kill on table", Pattern.CASE_INSENSITIVE), "kill on table"));
        result.add(new RegexDescription(Pattern.compile("(^|: )Kill(s)? "), "'Kill(s)' at start of sentence"));
        result.add(new RegexDescription(Pattern.compile("^kill[^es][^o][^n]", Pattern.CASE_INSENSITIVE), "Start of sentence"));

        // Restricting card kill actions
        result.add(new RegexDescription(Pattern.compile("may\\snot\\sotherwise\\skill[^eis]"), "may not otherwise kill"));
        result.add(new RegexDescription(Pattern.compile("may not be killed", Pattern.CASE_INSENSITIVE), "'may not be killed'"));
        result.add(new RegexDescription(Pattern.compile("may not enter kill", Pattern.CASE_INSENSITIVE), "'may not enter kill'"));
        result.add(new RegexDescription(Pattern.compile("[^efn]\\syou\\skill must be"), "'you kill must be'"));
        result.add(new RegexDescription(Pattern.compile("may only (seed or )?kill"), "may only (seed or) kill"));
        result.add(new RegexDescription(Pattern.compile("may only download .+[^t]\\sinto kill"), "may only download into kill"));

        // Unclear
        result.add(new RegexDescription(Pattern.compile("killing"), "killing"));
        result.add(new RegexDescription(Pattern.compile("(can|may)\\s(?!only)(not )?(immediately )?(.+ or )?(immediately )?kill[^es]", Pattern.CASE_INSENSITIVE), "can/may (not/immediately) (? or) (immediately) kill"));
        result.add(new RegexDescription(Pattern.compile("card\\skill[^eis]"), "card kill"));
        result.add(new RegexDescription(Pattern.compile("and\\s(\\(once per turn\\) )?kill[^eis]"), "and kill"));
        result.add(new RegexDescription(Pattern.compile("suspend(s)?(ing)?\\skill[^eis]", Pattern.CASE_INSENSITIVE), "suspend(s/ing) kill"));
        result.add(new RegexDescription(Pattern.compile("[^ny]\\skill for free"), "kill for free"));
        result.add(new RegexDescription(Pattern.compile("^(immediately )?kill on [^t]", Pattern.CASE_INSENSITIVE), "kill on"));
        result.add(new RegexDescription(Pattern.compile(" to kill\\s[^f]"), " to kill"));
        result.add(new RegexDescription(Pattern.compile("OR kill on any spaceline"), "OR kill on any spaceline"));
        result.add(new RegexDescription(Pattern.compile("kill or place in hand", Pattern.CASE_INSENSITIVE), "kill or place in hand"));
        result.add(new RegexDescription(Pattern.compile("kill Wormhole"), "kill Wormhole"));
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
        result.add(new RegexDescription(Pattern.compile("\\skill"), "Lowercase with a space"));
        result.add(new RegexDescription(Pattern.compile("[^\\s]kill"), "Lowercase with no space"));
        result.add(new RegexDescription(Pattern.compile("kill"), "Uppercase"));
        return result;
    }

}