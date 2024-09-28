package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.ActionContext;

public class DoNothingEffect extends UnrespondableEffect {

    public DoNothingEffect(ActionContext context) {
        super(context);
    }
    @Override
    protected void doPlayEffect() {
        // Do nothing
    }
}
