package com.gempukku.stccg.rules.tribbles;

import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.rules.generic.AbstractRule;

public abstract class TribblesRule extends AbstractRule {
    protected final TribblesGame _game;

    public TribblesRule(TribblesGame game) {
        _game = game;
    }
}