package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Characteristic;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class CharacteristicFilter implements Filter {

    private final Characteristic _characteristic;

    public CharacteristicFilter(Characteristic characteristic) {
        _characteristic = characteristic;
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return physicalCard.hasCharacteristic(_characteristic);
    }

}