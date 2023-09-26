package com.gempukku.lotro.effects;

import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.actions.SubAction;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.common.TribblePower;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.game.TribblesGame;
import com.gempukku.lotro.results.ActivateTribblePowerResult;
import com.gempukku.lotro.rules.GameUtils;

public abstract class ActivateTribblePowerEffect extends AbstractEffect {
    protected LotroPhysicalCard _source;
    protected String _activatingPlayer;
    protected TribblePower _tribblePower;
    protected ActivateTribblePowerResult _result;
    protected CostToEffectAction _action;
    protected DefaultActionContext<TribblesGame> _actionContext;
    public ActivateTribblePowerEffect(CostToEffectAction action, LotroPhysicalCard source, DefaultActionContext actionContext) {
        _source = source;
        _activatingPlayer = source.getOwner();
        _tribblePower = source.getBlueprint().getTribblePower();
        _action = action;
        _actionContext = actionContext;
        _result = new ActivateTribblePowerResult(_activatingPlayer, _tribblePower);
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return true;
    }

    @Override
    public String getText(DefaultGame game) {
        return "Activated " + GameUtils.getCardLink(_source);
    }

    public ActivateTribblePowerResult getActivateTribblePowerResult() {
        return _result;
    }

    public LotroPhysicalCard getSource() {
        return _source;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(DefaultGame game) {
        return new FullEffectResult(true);
    }

    protected FullEffectResult addActionAndReturnResult(DefaultGame game, SubAction subAction) {
        game.getActionsEnvironment().addActionToStack(subAction);
        game.getActionsEnvironment().emitEffectResult(_result);
        return new FullEffectResult(true);
    }
}
