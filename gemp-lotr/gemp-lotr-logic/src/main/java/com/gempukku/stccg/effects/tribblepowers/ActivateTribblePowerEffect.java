package com.gempukku.stccg.effects.tribblepowers;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.TribblePower;
import com.gempukku.stccg.effects.AbstractEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.results.ActivateTribblePowerResult;
import com.gempukku.stccg.rules.GameUtils;

public class ActivateTribblePowerEffect extends AbstractEffect<TribblesGame> {
    protected PhysicalCard _source;
    protected String _activatingPlayer;
    protected TribblePower _tribblePower;
    protected ActivateTribblePowerResult _result;
    protected CostToEffectAction _action;
    public ActivateTribblePowerEffect(CostToEffectAction action, PhysicalCard source) {
        _source = source;
        _activatingPlayer = source.getOwner();
        _tribblePower = source.getBlueprint().getTribblePower();
        _action = action;
        _result = new ActivateTribblePowerResult(_activatingPlayer, _tribblePower);
    }

    @Override
    public boolean isPlayableInFull(TribblesGame game) {
        return true;
    }

    @Override
    public String getText(DefaultGame game) {
        return "Activated " + GameUtils.getCardLink(_source);
    }

    public PhysicalCard getSource() {
        return _source;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(TribblesGame game) {
        return new FullEffectResult(true);
    }

    protected FullEffectResult addActionAndReturnResult(TribblesGame game, SubAction subAction) {
        game.getActionsEnvironment().addActionToStack(subAction);
        game.getActionsEnvironment().emitEffectResult(_result);
        return new FullEffectResult(true);
    }
}