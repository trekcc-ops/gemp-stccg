package com.gempukku.lotro.modifiers;

import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.common.Phase;

public interface ModifiersEnvironment {
    ModifierHook addAlwaysOnModifier(Modifier modifier);

    void addUntilStartOfPhaseModifier(Modifier modifier, Phase phase);

    void addUntilEndOfPhaseModifier(Modifier modifier, Phase phase);

    void addUntilEndOfTurnModifier(Modifier modifier);
    void addUntilEndOfPlayersNextTurnThisRoundModifier(Modifier modifier, String playerId);

    void addedWound(PhysicalCard card);

    int getWoundsTakenInCurrentPhase(PhysicalCard card);
}
