package com.gempukku.stccg.requirement.trigger;

import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.game.DefaultGame;

public interface TriggerChecker<AbstractGame extends DefaultGame> extends Requirement<AbstractGame> {
    boolean isBefore();
}
