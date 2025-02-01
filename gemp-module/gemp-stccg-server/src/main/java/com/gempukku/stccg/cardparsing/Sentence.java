package com.gempukku.stccg.cardparsing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Sentence implements GameTextObject {

    String text;
    Map<String, String> identifiedParts;

    Sentence(String string) {
        text = string;
    }

    @Override
    public boolean canBeParsed() {
        return LibraryFunctions.isSentenceUnderstood(text);
    }

    public List<Sentence> getSentences() {
        List<Sentence> result = new ArrayList<>();
        result.add(this);
        return result;
    }

    public String toString() {
        return text;
    }

    public void identifyPart(String partName, String partText) {
        if (identifiedParts.get(partName) != null)
            throw new RuntimeException("Already have " + partName);
        else {
            identifiedParts.put(partName, partText);

        }
    }

    public boolean contains(String play) {
        return text.contains(play);
    }
}