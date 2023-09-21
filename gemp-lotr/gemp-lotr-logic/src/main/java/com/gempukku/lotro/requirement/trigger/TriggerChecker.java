package com.gempukku.lotro.requirement.trigger;

import com.gempukku.lotro.requirement.Requirement;
import com.gempukku.lotro.game.DefaultGame;

public interface TriggerChecker<AbstractGame extends DefaultGame> extends Requirement<AbstractGame> {
    boolean isBefore();
}
