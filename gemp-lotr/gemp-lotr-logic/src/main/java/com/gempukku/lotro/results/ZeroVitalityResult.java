package com.gempukku.lotro.results;

import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.effects.EffectResult;

import java.util.Set;

public class ZeroVitalityResult extends EffectResult {
    private final Set<PhysicalCard> _characters;

    public ZeroVitalityResult(Set<PhysicalCard> characters) {
        super(Type.ZERO_VITALITY);
        _characters = characters;
    }

    public Set<PhysicalCard> getCharacters() {
        return _characters;
    }
}
