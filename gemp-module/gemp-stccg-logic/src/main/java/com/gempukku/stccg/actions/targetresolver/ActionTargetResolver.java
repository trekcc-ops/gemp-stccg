package com.gempukku.stccg.actions.targetresolver;

import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

public interface ActionTargetResolver {
    void resolve(DefaultGame cardGame) throws InvalidGameLogicException;
    boolean cannotBeResolved(DefaultGame cardGame);
    boolean isResolved();
}