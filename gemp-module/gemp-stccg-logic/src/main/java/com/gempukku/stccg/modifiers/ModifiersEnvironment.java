package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.Player;

public interface ModifiersEnvironment {
    ModifierHook addAlwaysOnModifier(Modifier modifier);

    void addUntilEndOfPhaseModifier(Modifier modifier, Phase phase);

    void addUntilEndOfTurnModifier(Modifier modifier);
    void addUntilEndOfPlayersNextTurnThisRoundModifier(Modifier modifier, String playerId);
    void signalEndOfTurn();

    void signalStartOfTurn();
    void useNormalCardPlay(Player player);

}