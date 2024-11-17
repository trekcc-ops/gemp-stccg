package com.gempukku.stccg.parsing;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.File;
import java.util.*;

public class NewLibraryTest {

    List<CardData> _newLibrary = new LinkedList<>();
    Map<String, CardData> _newLibraryMap = new HashMap<>();

    public void createLibrary() {
        File input;
        MappingIterator<Map<?, ?>> mappingIterator;
        List<Map<?, ?>> list;
        input = new File("..\\gemp-stccg-logic\\src\\test\\resources\\Physical.csv");
        try {
            CsvSchema csv = CsvSchema.emptySchema().withHeader();
            CsvMapper csvMapper = new CsvMapper();
            mappingIterator =  csvMapper.reader().forType(Map.class).with(csv).readValues(input);
            list = mappingIterator.readAll();
            for (Map<?, ?> card : list) {
                CardData cardInfo = new CardData(card, "Physical");
                if (!cardInfo._formats.contains("ban_2E")) {
                    String cardTitle = cardInfo._title.replace(" *VP","");
                    if (_newLibraryMap.get(cardTitle) == null) {
                        _newLibraryMap.put(cardTitle, cardInfo);
                    } else {
                        if (!Objects.equals(_newLibraryMap.get(cardTitle)._rawGameText, cardInfo._rawGameText)) {
                            String newMapName = cardTitle + " (" + cardInfo._set + ")";
                            if (_newLibraryMap.get(newMapName) != null)
                                throw new RuntimeException("Shouldn't have happened");
                            else _newLibraryMap.put(newMapName, cardInfo);
                        }
                    }
                    _newLibrary.add(cardInfo);
                }
            }

            input = new File("..\\gemp-stccg-logic\\src\\test\\resources\\Virtual.csv");
            mappingIterator =  csvMapper.reader().forType(Map.class).with(csv).readValues(input);
            list = mappingIterator.readAll();
            for (Map<?, ?> card : list) {
                CardData cardInfo = new CardData(card, "Virtual");
                if (!cardInfo._formats.contains("ban_2E")) {
                    String cardTitle = cardInfo._title.replace(" *VP","");
                    if (_newLibraryMap.get(cardTitle) == null) {
                        _newLibraryMap.put(cardTitle, cardInfo);
                    } else {
                        if (!Objects.equals(_newLibraryMap.get(cardTitle)._rawGameText, cardInfo._rawGameText)) {
                            String newMapName = cardTitle + " (" + cardInfo._set + ")";
                            if (_newLibraryMap.get(newMapName) != null)
                                throw new RuntimeException("Shouldn't have happened");
                            else _newLibraryMap.put(newMapName, cardInfo);
                        }
                    }
                    _newLibrary.add(cardInfo);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public class CardData {

        String _title;
        String _rawGameText;
        GameTextObject _gameText;
        String _type;
        String _set;
        String _formats;
        String _csvSource;

        CardData(Map<?, ?> card, String csvSource) {
            _title = card.get("Name").toString();
            _formats = card.get("Set").toString();
            _rawGameText = card.get("Text").toString().replace("{", "").replace("}", "");
            _set = card.get("Release").toString();
            _type = card.get("Type").toString();
            _csvSource = csvSource;
            List<String> gameTextPieces = Arrays.asList(_rawGameText.split("(" + endOfSentenceFollowedBySpace() +
                    "OR)"));
            if (gameTextPieces.size() > 1) {
                _gameText = new ChooseOptionGameText(gameTextPieces);
            }
            else _gameText = new GameText(_rawGameText);
        }
    }

    public List<String> splitIntoSentences(String text) {
        String periodFollowedBySpace = "(?<=\\.\\s)";
        String notUSS = "(?<!U\\.S\\.S\\.\\s)";
        String notIKC = "(?<!I\\.K\\.C\\.\\s)";
        String notIKS = "(?<!I\\.K\\.S\\.\\s)";
        String notFollowedByLowercase = "(?![a-z])";
        String notFollowedByOrAndLowercase = "(?!OR\\s[a-z])";
        String notDr = "(?<!Dr\\.\\s)";
        String notvs = "(?<!\svs\\.\\s)";
        String fullRegex =
                periodFollowedBySpace + notUSS + notIKC + notIKS + notFollowedByLowercase +
                        notFollowedByOrAndLowercase + notDr + notvs;
        String[] splitString = text.split(fullRegex);
        List<String> result = new ArrayList<>();
        for (String string : splitString) {
            result.add(string.strip());
        }
        return result;
    }

    public String endOfSentenceFollowedBySpace() {
        String periodFollowedBySpace = "(?<=\\.\\s)";
        String notUSS = "(?<!U\\.S\\.S\\.\\s)";
        String notIKC = "(?<!I\\.K\\.C\\.\\s)";
        String notIKS = "(?<!I\\.K\\.S\\.\\s)";
        String notFollowedByLowercase = "(?![a-z])";
        String notFollowedByOrAndLowercase = "(?!OR\\s[a-z])";
        String notDr = "(?<!Dr\\.\\s)";
        String notvs = "(?<!\svs\\.\\s)";
        return periodFollowedBySpace + notUSS + notIKC + notIKS +
                notFollowedByLowercase + notFollowedByOrAndLowercase + notDr + notvs;
    }

    public class GameText implements GameTextObject {

        List<Sentence> sentences = new ArrayList<>();

        GameText(String text) {
            this(splitIntoSentences(text));
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

    public class ChooseOptionGameText implements GameTextObject {

        List<GameText> gameTextOptions = new LinkedList<>();
        String afterAnyUse;
        boolean captainsOrder;

        ChooseOptionGameText(List<String> pieces) {
            List<List<String>> allPiecesSentences = new ArrayList<>();
            for (String piece : pieces) {
                List<String> pieceSentences = splitIntoSentences(piece);
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

    interface GameTextObject {

        boolean canBeParsed();
        List<Sentence> getSentences();
    }

    public class Sentence implements GameTextObject {

        String text;

        Sentence(String string) {
            text = string;
            if (string.contains(";")) {
                List<String> splitString = Arrays.asList(string.split(";"));

            }
        }
        @Override
        public boolean canBeParsed() {
            return isSentenceUnderstood(text);
        }

        public List<Sentence> getSentences() {
            List<Sentence> result = new ArrayList<>();
            result.add(this);
            return result;
        }

        public String toString() {
            return text;
        }

    }

    boolean isSentenceUnderstood(String sentence) {
        String fullSentence = sentence;
        if (fullSentence.endsWith(".")) {
            fullSentence = fullSentence.substring(0, fullSentence.length() - 1);
        }

        // Places where you can play or seed a card. May require selection, but no antecedent or response required.
        String [] validPlayOnTargets = {
                "table",
                "your unattempted mission",
                "your space facility",
                "your Q's Tent",
                "your ship",
                "any Bajoran mission",
                "any Cardassian mission",
                "any Federation mission",
                "any Klingon mission",
                "an opponent's [P] mission",
                "a planet mission",
                "a mission",
                "a homeworld",
                "Quark's Bar",
                "Ferengi Trading Post",
                "a personnel you own who is a captive OR under opponent's control",
                "your Acquisition personnel",
                "your Nagus",
                "Ops",
                "one personnel you've captured",
                "any Klingon who survived a losing battle",
                "any one Klingon",
                "any non-aligned ship",
                "a mission in a region",
                "your Acquisition personnel in their native quadrant",
                "any one personnel",
                "your Borg, android or any Geordi",
                "one of your personnel",
                "John Doe after he has prevented a death",
                "your [Fer] ship",
                "a mission with \"array\" or \"listening post\" in lore",
                "your [MQ] ship",
                "a non-Borg personnel who has INTEGRITY<8 and no Honor",
                "a Cardassia Region [P]",
                "a [S] location",
                "your [Vul] personnel",
                "any Vulcan mission",
                "any [VUL] mission",
                "any Non-Aligned mission (or any mission with \"Andorian\" in lore)",
                "your personnel with Navigation x2 or Stellar Cartography x2",
                "your Borg",
                "any ship",
                "a Neutral Zone Region mission",
                "a time location",
                "your [Orb] personnel",
                "your Mindmeld personnel",
                "your undamaged ship with a Cloaking Device",
                "your Reman",
                "an Ore Processing Unit",
                "your non-[Fed] ship",
                "your infiltrator",
                "Calder II",
                "Veytan",
                "your Klingon"
        };

        List<String> fullSentences = new ArrayList<>();
        fullSentences.add("(Captain's Order.)");
        fullSentences.add("(Unique.)");
        fullSentences.add("(Cumulative.)");
        fullSentences.add("Discard objective after use");

        List<String> playOnSentenceBeginnings = new ArrayList<>();
        playOnSentenceBeginnings.add("Plays on");
        playOnSentenceBeginnings.add("Seeds or plays on");
        playOnSentenceBeginnings.add("Seeds on");
        playOnSentenceBeginnings.add("Seed one on");

        for (String string : playOnSentenceBeginnings) {
            if (fullSentence.startsWith(string)) {
                String[] sentenceParts =
                        fullSentence.split("(?<=" + string.replace(" ","\\s") + ")");
                String target = sentenceParts[1].strip();
                if (sentenceParts.length == 2 && Arrays.stream(validPlayOnTargets).toList().contains(target))
                    return true;
                String[] targetParts = target.split("(\\sor\\s)");
                if (areValidTargets(targetParts, validPlayOnTargets))
                    return true;
            }
        }

        return fullSentences.contains(fullSentence);
    }

    private boolean areValidTargets(String[] strings, String[] validTargets) {
        boolean result = true;
        for (String string : strings) {
            if (!Arrays.stream(validTargets).toList().contains(string))
                result = false;
        }
        return result;
    }



}