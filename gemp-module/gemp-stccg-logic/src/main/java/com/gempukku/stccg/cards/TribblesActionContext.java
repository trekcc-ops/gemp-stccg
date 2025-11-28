package com.gempukku.stccg.cards;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.TribblesGame;

import java.util.Objects;

public class TribblesActionContext extends DefaultActionContext {
    protected final ActionContext _relevantContext;


    public TribblesActionContext(ActionContext delegate, String performingPlayer,
                                 PhysicalCard source, ActionResult actionResult) {
        super(performingPlayer, source, actionResult);
        _relevantContext = Objects.requireNonNullElse(delegate, this);
    }

    @Override
    protected ActionContext getRelevantContext() { return _relevantContext; }

}