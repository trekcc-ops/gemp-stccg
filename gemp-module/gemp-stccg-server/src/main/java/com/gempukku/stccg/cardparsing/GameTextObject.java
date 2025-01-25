package com.gempukku.stccg.cardparsing;

import java.util.List;

interface GameTextObject {

    boolean canBeParsed();

    List<Sentence> getSentences();
}