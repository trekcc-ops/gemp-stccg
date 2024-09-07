package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.lotr.PossessionClass;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

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
