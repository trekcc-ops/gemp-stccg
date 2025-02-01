package com.gempukku.stccg.cardparsing;

import java.util.regex.Matcher;

public class RegexResult {
    String _sentence;
    String _cardTitle;
    RegexDescription _regexDescription;
    int _start;
    int _end;

    public RegexResult(String cardTitle, String sentence, int start, int end,
                       RegexDescription regexDescription) {
        _cardTitle = cardTitle;
        _regexDescription = regexDescription;
        _sentence = sentence;
        _start = start;
        _end = end;
    }

    public String getSentenceWithHighlights() {
        StringBuilder stringBuilder = new StringBuilder();
        if (_start == 0)
            stringBuilder.append("<<");
        else {
            stringBuilder.append(_sentence.substring(0,_start));
            stringBuilder.append("<<");
        }
        stringBuilder.append(_sentence.substring(_start,_end));
        stringBuilder.append(">>");
        stringBuilder.append(_sentence.substring(_end, _sentence.length()));
        return stringBuilder.toString();
    }
}