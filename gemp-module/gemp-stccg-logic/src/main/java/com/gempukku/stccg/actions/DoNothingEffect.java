package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;

public class DoNothingEffect extends UnrespondableEffect {

    public DoNothingEffect(ActionContext context) {
        super(context);
    }

    public DoNothingEffect(DefaultGame game) {
        super(game);
    }
    @Override
    protected void doPlayEffect() {
        // Do nothing
    }
}