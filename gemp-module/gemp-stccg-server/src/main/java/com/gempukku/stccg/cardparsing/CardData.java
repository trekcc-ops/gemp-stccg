package com.gempukku.stccg.cardparsing;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
        if (_rawGameText.contains("James T.")) {
            int x = 5;
            int y = x + 3;
        }
        if (gameTextPieces.size() > 1) {
            _gameText = new ChooseOptionGameText(gameTextPieces);
        }
        else _gameText = new GameText(_rawGameText);
    }

    public String endOfSentenceFollowedBySpace() {
        String periodFollowedBySpace = "(?<=\\.\\s)";
        String notUSS = "(?<!U\\.S\\.S\\.\\s)";
        String notIKC = "(?<!I\\.K\\.C\\.\\s)";
        String notIKS = "(?<!I\\.K\\.S\\.\\s)";
        String notFollowedByLowercase = "(?![a-z])";
        String notFollowedByOrAndLowercase = "(?!OR\\s[a-z])";
        String notDr = "(?<!Dr\\.\\s)";
        String notJamesT = "(?<!\\sT.\\s)";
        String notvs = "(?<!\\svs\\.\\s)";
        return periodFollowedBySpace + notUSS + notIKC + notIKS +
                notFollowedByLowercase + notFollowedByOrAndLowercase + notDr + notvs + notJamesT;
    }


}