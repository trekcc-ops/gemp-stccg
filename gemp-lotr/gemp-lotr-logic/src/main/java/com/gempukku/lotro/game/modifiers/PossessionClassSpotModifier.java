package com.gempukku.lotro.game.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.PossessionClass;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.DefaultGame;

public class PossessionClassSpotModifier extends AbstractModifier {
    private final PossessionClass _possessionClass;

    public PossessionClassSpotModifier(PhysicalCard source, PossessionClass possessionClass) {
        super(source, "Spotting modifier", null, ModifierEffect.SPOT_MODIFIER);
        _possessionClass = possessionClass;
    }


    @Override
    public int getSpotCountModifier(DefaultGame game, Filterable filter) {
        if (filter == _possessionClass)
            return 1;
        return 0;
    }
}