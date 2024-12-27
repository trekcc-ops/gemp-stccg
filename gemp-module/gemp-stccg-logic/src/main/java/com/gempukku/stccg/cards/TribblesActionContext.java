package com.gempukku.stccg.cards;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.gamestate.TribblesGameState;

import java.util.Objects;

public class TribblesActionContext extends DefaultActionContext {
    private final TribblesGame _game;
    protected final ActionContext _relevantContext;


    public TribblesActionContext(ActionContext delegate, String performingPlayer, TribblesGame game,
                                 PhysicalCard source, ActionResult actionResult) {
        super(performingPlayer, game, source, actionResult);
        _game = game;
        _relevantContext = Objects.requireNonNullElse(delegate, this);
    }

    @Override
    protected ActionContext getRelevantContext() { return _relevantContext; }

    @Override
    public TribblesGame getGame() {
        return _game;
    }

    @Override
    public TribblesGameState getGameState() { return _game.getGameState(); }

    @Override
    public ActionContext createDelegateContext(ActionResult actionResult) {
        return new TribblesActionContext(this, getPerformingPlayerId(), getGame(), getSource(), actionResult);
    }


}