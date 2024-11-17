package com.gempukku.stccg.parsing;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class SentenceClassifyTest extends NewLibraryTest {

    public void sentenceTest() throws IOException {
        createLibrary();
        int canBeParsed = 0;
        int cannotBeParsed = 0;

        List<String> includedTypes = new LinkedList<>();
        includedTypes.add("Event");
        includedTypes.add("Incident");
        includedTypes.add("Interrupt");

        for (CardData card : _newLibraryMap.values()) {
            if (includedTypes.contains(card._type) && Objects.equals(card._set, "TNG")) {
                for (Sentence sentence : card._gameText.getSentences()) {
                    if (canBeClassified(sentence.toString())) {
                        canBeParsed++;
                    } else {
                        System.out.println(sentence);
                        cannotBeParsed++;
                    }
                }
            }
        }
        System.out.println("Can be parsed: " + canBeParsed);
        System.out.println("Cannot be parsed: " + cannotBeParsed);
    }

    public boolean canBeClassified(String sentence) {
        if (isPlayOnSentence(sentence))
            return true;
        return false;
    }

    public boolean isPlayOnSentence(String sentence) {
        List<String> playOnSentenceBeginnings = new ArrayList<>();
        playOnSentenceBeginnings.add("Plays on");
        playOnSentenceBeginnings.add("Seeds or plays on");
        playOnSentenceBeginnings.add("Seeds on");
        playOnSentenceBeginnings.add("Seed one on");

        for (String string : playOnSentenceBeginnings) {
            if (sentence.startsWith(string)) {
                return true;
            }
        }
        return false;
    }



    public void sentenceNLPTest() throws IOException {
        String fullText = "Plays once each turn on your [DS9][Fer] personnel. Name a skill.";
        InputStream inputStream = new FileInputStream("..\\gemp-stccg-logic\\src\\test\\resources\\en-sent.bin");
        SentenceModel model = new SentenceModel(inputStream);
        SentenceDetectorME parser = new SentenceDetectorME(model);
        String[] sentences = parser.sentDetect(fullText);
        for (String sentence : sentences) {
            System.out.println(sentence);
        }
    }

}