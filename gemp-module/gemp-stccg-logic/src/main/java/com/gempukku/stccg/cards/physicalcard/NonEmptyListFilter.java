package com.gempukku.stccg.cards.physicalcard;

import java.util.Arrays;
import java.util.List;

public class NonEmptyListFilter {
    @Override
    public boolean equals(Object other) {
        // Trick required to be compliant with the Jackson Custom attribute processing 
        if (other == null) {
            return true;
        }


        if (other instanceof List<?> list) {
            return list.isEmpty();
        } else if (other instanceof Object[] array) {
            return array.length == 0;
        } else {
            return true;
        }
    }
}