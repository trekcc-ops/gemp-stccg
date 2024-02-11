package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;

import java.util.List;

public interface ModifiersEnvironment {
    ModifierHook addAlwaysOnModifier(Modifier modifier);

    void addUntilStartOfPhaseModifier(Modifier modifier, Phase phase);

    void addUntilEndOfPhaseModifier(Modifier modifier, Phase phase);

    void addUntilEndOfTurnModifier(Modifier modifier);
    void addUntilEndOfPlayersNextTurnThisRoundModifier(Modifier modifier, String playerId);

}
