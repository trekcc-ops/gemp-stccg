package com.gempukku.lotro.cards.build.field.effect.trigger;

import com.gempukku.lotro.cards.build.Requirement;
import com.gempukku.lotro.game.DefaultGame;

public interface TriggerChecker<AbstractGame extends DefaultGame> extends Requirement<AbstractGame> {
    boolean isBefore();
}
