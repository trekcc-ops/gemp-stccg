package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.effects.defaulteffect.ActivateCardEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.GameUtils;
import com.gempukku.stccg.effects.Effect;

public class ActivateCardAction extends AbstractCostToEffectAction {

    protected final PhysicalCard _physicalCard;
    protected ActivateCardEffect _activateCardEffect;
    protected boolean _sentMessage;
    protected boolean _activated;
    protected boolean _prevented;
    protected final DefaultGame _game;

    public ActivateCardAction(DefaultGame game, PhysicalCard physicalCard) {
        _physicalCard = physicalCard;
        _game = game;
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
                _activateCardEffect = new ActivateCardEffect(game, _physicalCard);
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
