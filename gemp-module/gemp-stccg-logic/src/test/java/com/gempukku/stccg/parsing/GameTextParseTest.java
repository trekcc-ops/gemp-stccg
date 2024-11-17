package com.gempukku.stccg.parsing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameTextParseTest extends NewLibraryTest {


    void getCsv() throws Exception {
        createLibrary();
        int understood = 0;
        int notUnderstood = 0;

        Map<String, Integer> setCounts = new HashMap<>();

        for (CardData card : _newLibrary) {
            Integer currentCount = setCounts.get(card._set);
            if (currentCount == null)
                currentCount = 0;
            currentCount++;
            setCounts.put(card._set, currentCount);

//            if (card._type.equals("Event") || card._type.equals("Incident")) {
            if (!card._type.equals("Personnel") && !card._type.equals("Mission") &&
                    !card._type.equals("Dilemma") && !card._type.equals("Ship") && !card._type.equals("Tactic")) {

                List<String> splitText = splitIntoSentences(card._rawGameText);

                for (String string : splitText) {
                    String sentence = string.trim();
//                        String sentence = string + ((string.endsWith(".)") || string.endsWith(".")) ? "" : ".");
                    if (!isSentenceUnderstood(sentence)) {
                        System.out.println(sentence);
                        notUnderstood++;
                    } else {
//                        System.out.println("ok");
                        understood++;
                    }
                }
            }
        }
/*
        for (String release : setCounts.keySet()) {
            System.out.println(release + ": " + setCounts.get(release) + " cards");
        }
*/
        System.out.println(understood + " understood");
        System.out.println(notUnderstood + " not understood");
    }
}