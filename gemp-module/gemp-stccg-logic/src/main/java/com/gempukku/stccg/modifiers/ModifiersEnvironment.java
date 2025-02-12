package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

public interface ModifiersEnvironment {
    ModifierHook addAlwaysOnModifier(Modifier modifier);

    void addUntilEndOfPhaseModifier(Modifier modifier, Phase phase);

    void addUntilEndOfTurnModifier(Modifier modifier);

    void signalEndOfTurn();

    void useNormalCardPlay(Player player);
    void removeModifierHooks(PhysicalCard card);
    void addModifierHooks(PhysicalCard card);

    void addModifierHooks(DefaultGame cardGame, PhysicalCard card);

}