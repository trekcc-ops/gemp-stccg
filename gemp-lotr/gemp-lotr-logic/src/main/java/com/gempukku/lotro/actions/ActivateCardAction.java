package com.gempukku.lotro.actions;

import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.effects.ActivateCardEffect;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.rules.GameUtils;
import com.gempukku.lotro.effects.Effect;

public class ActivateCardAction extends AbstractCostToEffectAction<DefaultGame> {

    protected final PhysicalCard _physicalCard;
    protected ActivateCardEffect _activateCardEffect;
    protected boolean _sentMessage;
    protected boolean _activated;
    protected boolean _prevented;

    public ActivateCardAction(PhysicalCard physicalCard) {
        _physicalCard = physicalCard;
        setText("Use " + GameUtils.getFullName(_physicalCard));
    }

    @Override
    public ActionType getActionType() {
        return ActionType.SPECIAL_ABILITY;
    }

    @Override
    public PhysicalCard getActionSource() {
        return _physicalCard;
    }

    @Override
    public PhysicalCard getActionAttachedToCard() {
        return _physicalCard;
    }

    public void prevent() {
        _prevented = true;
    }

    @Override
    public Effect nextEffect(DefaultGame game) {
        if (!_sentMessage) {
            _sentMessage = true;
            if (_physicalCard != null && _physicalCard.getZone().isInPlay()) {
                game.getGameState().activatedCard(getPerformingPlayer(), _physicalCard);
                game.getGameState().sendMessage(GameUtils.getCardLink(_physicalCard) + " is used");
            }
        }

        if (!isCostFailed()) {
            Effect cost = getNextCost();
            if (cost != null)
                return cost;

            if (!_activated) {
                _activated = true;
                _activateCardEffect = new ActivateCardEffect(_physicalCard, getActionTimeword());
                return _activateCardEffect;
            }

            if (_activateCardEffect.getActivateCardResult().isEffectCancelled())
                return null;
            if (!_prevented)
                return getNextEffect();
        }
        return null;
    }
}
