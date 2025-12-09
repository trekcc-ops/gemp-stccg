package com.gempukku.stccg.modifiers;

public class UntilEndOfTurnModifierTiming extends ModifierTiming {

    private final int _turnNumber;
    private final ModifierTimingType _type = ModifierTimingType.UNTIL_END_OF_THIS_TURN;

    public UntilEndOfTurnModifierTiming(int turnNumber) {
        _turnNumber = turnNumber;
    }
}