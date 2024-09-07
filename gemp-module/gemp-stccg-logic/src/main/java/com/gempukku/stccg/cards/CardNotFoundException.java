package com.gempukku.stccg.cards;

public class CardNotFoundException extends Exception {
    public CardNotFoundException(String blueprint) {
        super(blueprint);
    }
}
