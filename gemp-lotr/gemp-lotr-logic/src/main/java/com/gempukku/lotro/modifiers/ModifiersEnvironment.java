package com.gempukku.lotro.modifiers;

import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.common.Phase;

public interface ModifiersEnvironment {
    ModifierHook addAlwaysOnModifier(Modifier modifier);

    void addUntilStartOfPhaseModifier(Modifier modifier, Phase phase);

    void addUntilEndOfPhaseModifier(Modifier modifier, Phase phase);

    void addUntilEndOfTurnModifier(Modifier modifier);

    void addedWound(LotroPhysicalCard card);

    int getWoundsTakenInCurrentPhase(LotroPhysicalCard card);
}
