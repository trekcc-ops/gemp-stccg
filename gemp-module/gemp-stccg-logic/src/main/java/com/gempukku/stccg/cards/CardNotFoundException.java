package com.gempukku.stccg.cards;

import java.io.IOException;

public class CardNotFoundException extends IOException {
    public CardNotFoundException(String blueprint) {
        super(blueprint);
    }
}