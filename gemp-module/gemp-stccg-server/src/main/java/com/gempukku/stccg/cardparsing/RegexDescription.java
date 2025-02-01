package com.gempukku.stccg.cardparsing;

import java.util.regex.Pattern;

public class RegexDescription {
    Pattern _regex;
    String _description;

    RegexDescription(Pattern regex, String description) {
        _regex = regex;
        _description = description;
    }

    public Pattern getRegex() {
        return _regex;
    }
}