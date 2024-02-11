package com.gempukku.stccg.effects.defaulteffect;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.common.filterable.TribblePower;
import com.gempukku.stccg.effects.DefaultEffect;
import com.gempukku.stccg.effects.utils.EffectType;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.results.ActivateTribblePowerResult;
import com.gempukku.stccg.rules.GameUtils;

public abstract class ActivateTribblePowerEffect extends DefaultEffect {
    protected final PhysicalCard _source;
    protected final String _activatingPlayer;
    protected final TribblePower _tribblePower;
    protected final ActivateTribblePowerResult _result;
    protected final CostToEffectAction _action;
    protected final TribblesGame _game;
    public ActivateTribblePowerEffect(CostToEffectAction action, TribblesActionContext actionContext) {
        _source = actionContext.getSource();
        _activatingPlayer = actionContext.getSource().getOwnerName();
        _tribblePower = actionContext.getSource().getBlueprint().getTribblePower();
        _game = actionContext.getGame();
        _action = action;
        _result = new ActivateTribblePowerResult(_activatingPlayer, _tribblePower);
    }

    @Override
    public boolean isPlayableInFull() {
        return true;
    }
    @Override
    public EffectType getType() { return EffectType.ACTIVATE_TRIBBLE_POWER; }

    @Override
    public String getText() {
        return "Activated " + GameUtils.getCardLink(_source);
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
}