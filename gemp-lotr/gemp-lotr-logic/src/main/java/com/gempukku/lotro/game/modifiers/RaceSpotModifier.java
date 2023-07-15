package com.gempukku.lotro.game.modifiers;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.DefaultGame;

public class RaceSpotModifier extends AbstractModifier {
    private final Race _race;

    public RaceSpotModifier(PhysicalCard source, Race race) {
        super(source, "Spotting modifier", null, ModifierEffect.SPOT_MODIFIER);
        _race = race;
    }


    @Override
    public int getSpotCountModifier(DefaultGame game, Filterable filter) {
        if (filter == _race)
            return 1;
        return 0;
    }
}