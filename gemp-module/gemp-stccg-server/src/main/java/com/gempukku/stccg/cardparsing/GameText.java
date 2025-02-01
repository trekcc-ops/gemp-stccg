package com.gempukku.stccg.cardparsing;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class GameText implements GameTextObject {

    List<Sentence> sentences = new ArrayList<>();

    GameText(String text) {
        this(LibraryFunctions.splitIntoSentences(text));
    }

    GameText(List<String> sentences) {
        for (String string : sentences)
            this.sentences.add(new Sentence(string));
    }


    public boolean canBeParsed() {
        boolean result = true;
        for (Sentence sentence : sentences) {
            if (!sentence.canBeParsed())
                result = false;
        }
        return result;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        for (Sentence sentence : sentences)
            sj.add(sentence.toString());
        return sj.toString();
    }

    public List<Sentence> getSentences() {
        return sentences;
    }
}