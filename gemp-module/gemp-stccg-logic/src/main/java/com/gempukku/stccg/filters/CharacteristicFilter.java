package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Characteristic;
import com.gempukku.stccg.game.DefaultGame;

public class CharacteristicFilter implements CardFilter {

    private final Characteristic _characteristic;

    public CharacteristicFilter(Characteristic characteristic) {
        _characteristic = characteristic;
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return physicalCard.hasCharacteristic(_characteristic);
    }

}