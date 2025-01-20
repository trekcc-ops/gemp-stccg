package com.gempukku.stccg.cards.physicalcard;

public class PositiveIntegerFilter {
    @Override
    public boolean equals(Object other) {
        // Trick required to be compliant with the Jackson Custom attribute processing 
        if (other == null) {
            return true;
        }
        int value = (Integer)other;
        return value < 0;
    }
}