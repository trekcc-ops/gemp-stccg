package com.gempukku.lotro.game.effects;

import com.gempukku.lotro.game.DefaultGame;

public class DoNothingEffect extends UnrespondableEffect {
    @Override
    protected void doPlayEffect(DefaultGame game) {
        // Do nothing
    }
}