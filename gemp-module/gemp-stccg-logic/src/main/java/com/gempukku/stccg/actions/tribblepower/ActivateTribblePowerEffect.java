package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.game.TribblesGame;

public abstract class ActivateTribblePowerEffect extends DefaultEffect {
    protected final PhysicalCard _source;
    protected final String _activatingPlayer;
    protected final TribblePower _tribblePower;
    protected final ActivateTribblePowerResult _result;
    protected final Action _action;
    protected final TribblesActionContext _context;
    protected final TribblesGame _tribblesGame; // TODO - Redundant with "_game" in DefaultEffect
    public ActivateTribblePowerEffect(Action action, TribblesActionContext actionContext) {
        super(actionContext.getGame(), actionContext.getSource().getOwnerName());
        _context = actionContext;
        _source = actionContext.getSource();
        _activatingPlayer = actionContext.getSource().getOwnerName();
        _tribblePower = actionContext.getSource().getBlueprint().getTribblePower();
        _action = action;
        _result = new ActivateTribblePowerResult(this);
        _tribblesGame = actionContext.getGame();
    }

    @Override
    public boolean isPlayableInFull() {
        return true;
    }
    @Override
    public EffectType getType() { return EffectType.ACTIVATE_TRIBBLE_POWER; }

    @Override
    public String getText() {
        return "Activated " + _source.getCardLink();
    }

    public PhysicalCard getSource() {
        return _source;
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        return new FullEffectResult(true);
    }

    protected FullEffectResult addActionAndReturnResult(TribblesGame game, SubAction subAction) {
        game.getActionsEnvironment().addActionToStack(subAction);
        game.getActionsEnvironment().emitEffectResult(_result);
        return new FullEffectResult(true);
    }

    @Override
    public TribblesGame getGame() { return _tribblesGame; }
}