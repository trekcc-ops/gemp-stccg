package com.gempukku.lotro.effects;

import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.actions.SubAction;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.common.TribblePower;
import com.gempukku.lotro.game.TribblesGame;
import com.gempukku.lotro.results.ActivateTribblePowerResult;
import com.gempukku.lotro.rules.GameUtils;

public class ActivateTribblePowerEffect extends AbstractEffect<TribblesGame> {
    protected LotroPhysicalCard _source;
    protected String _activatingPlayer;
    protected TribblePower _tribblePower;
    protected ActivateTribblePowerResult _result;
    protected CostToEffectAction _action;
    public ActivateTribblePowerEffect(CostToEffectAction action, LotroPhysicalCard source) {
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
    public String getText(TribblesGame game) {
        return "Activated " + GameUtils.getCardLink(_source);
    }

    public LotroPhysicalCard getSource() {
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