package com.gempukku.lotro.effects;

import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.common.TribblePower;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.results.ActivateTribblePowerResult;
import com.gempukku.lotro.rules.GameUtils;

public abstract class ActivateTribblePowerEffect extends AbstractEffect {
    protected LotroPhysicalCard _source;
    protected String _activatingPlayer;
    protected TribblePower _tribblePower;
    protected ActivateTribblePowerResult _result;
    public ActivateTribblePowerEffect(LotroPhysicalCard source) {
        _source = source;
        _activatingPlayer = source.getOwner();
        _tribblePower = source.getBlueprint().getTribblePower();
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
}
