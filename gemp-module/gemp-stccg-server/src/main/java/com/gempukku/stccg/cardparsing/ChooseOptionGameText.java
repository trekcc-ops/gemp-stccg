package com.gempukku.stccg.cardparsing;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ChooseOptionGameText implements GameTextObject {

    List<GameText> gameTextOptions = new LinkedList<>();
    String afterAnyUse;
    boolean captainsOrder;

    ChooseOptionGameText(List<String> pieces) {
        List<List<String>> allPiecesSentences = new ArrayList<>();
        for (String piece : pieces) {
            List<String> pieceSentences = LibraryFunctions.splitIntoSentences(piece);
            allPiecesSentences.add(pieceSentences);
        }
        List<String> copyString = new ArrayList<>();
        for (String sentence : allPiecesSentences.getLast()) {
            if (sentence.contains("after any use") || sentence.contains("after either use")) {
                afterAnyUse = sentence;
            } else if ("(Captain's Order.)".equals(sentence)) {
                captainsOrder = true;
            } else {
                copyString.add(sentence);
            }
        }
        allPiecesSentences.removeLast();
        allPiecesSentences.add(copyString);

        for (List<String> sentences : allPiecesSentences) {
            List<String> copyList = new ArrayList<>(sentences);
            if (afterAnyUse != null) {
                String toAdd = afterAnyUse.replace(
                        " after any use", "").replace(" after either use", "");
                copyList.add(toAdd);
            }
            gameTextOptions.add(new GameText(copyList));
        }
    }


    @Override
    public boolean canBeParsed() {
        boolean result = true;
        for (GameText gameText : gameTextOptions)
            if (!gameText.canBeParsed())
                result = false;
        return result;
    }

    public String toString() {
        return gameTextOptions.toString();
    }

    public List<Sentence> getSentences() {
        List<Sentence> result = new ArrayList<>();
        for (GameText text : gameTextOptions)
            result.addAll(text.getSentences());
        return result;
    }
}