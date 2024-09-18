package com.gempukku.stccg.actions.lotr;

import com.gempukku.stccg.actions.AbstractCostToEffectAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.actions.discard.DiscountEffect;

import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.actions.Effect;

import java.util.Collections;

public class PlayEventAction extends AbstractCostToEffectAction {
    private final PhysicalCard _eventPlayed;
    private final boolean _requiresRanger;

    private boolean _cardRemoved;

    private PlayEventEffect _playCardEffect;
    private boolean _cardPlayed;

    private boolean _cardDiscarded;

    private boolean _discountResolved;
    private boolean _discountApplied;

    private final Zone _playedFrom;
    private final DefaultGame _game;

    public PlayEventAction(PhysicalCard card) {
        this(card, false);
    }

    public PlayEventAction(PhysicalCard card, boolean requiresRanger) {
        _game = card.getGame();
        _eventPlayed = card;
        _requiresRanger = requiresRanger;

        _playedFrom = card.getZone();

        setText("Play " + _eventPlayed.getFullName());
    }

    public PhysicalCard getEventPlayed() {
        return _eventPlayed;
    }

    @Override
    public ActionType getActionType() {
        return ActionType.PLAY_CARD;
    }

    public boolean isRequiresRanger() {
        return _requiresRanger;
    }

    @Override
    public PhysicalCard getActionSource() {
        return _eventPlayed;
    }

    @Override
    public PhysicalCard getActionAttachedToCard() {
        return _eventPlayed;
    }

    @Override
    public Effect nextEffect() {
        if (!_cardRemoved) {
            _cardRemoved = true;
            final Zone playedFromZone = _eventPlayed.getZone();

            _game.sendMessage(_eventPlayed.getOwnerName() + " plays " + _eventPlayed.getCardLink() +
                    " from " + playedFromZone.getHumanReadable());
            _game.getGameState().removeCardsFromZone(_eventPlayed.getOwnerName(), Collections.singleton(_eventPlayed));

            if (playedFromZone == Zone.HAND)
                _game.getGameState().addCardToZone(_eventPlayed, Zone.VOID_FROM_HAND);
            else
                _game.getGameState().addCardToZone(_eventPlayed, Zone.VOID);

            if (playedFromZone == Zone.DRAW_DECK) {
                _game.sendMessage(_eventPlayed.getOwnerName() + " shuffles their deck");
                _game.getGameState().shuffleDeck(_eventPlayed.getOwnerName());
            }

            _game.getGameState().eventPlayed(_eventPlayed);
        }

        if (!_discountResolved) {
            final DiscountEffect discount = getNextPotentialDiscount();
            if (discount != null) {
                return discount;
            } else {
                _discountResolved = true;
            }
        }

        if (!_discountApplied) {
            _discountApplied = true;
            int twilightModifier = -getProcessedDiscount();
            insertCost(new PayTwilightCostEffect(_game, _eventPlayed, twilightModifier));
        }

        if (!isCostFailed()) {
            Effect cost = getNextCost();
            if (cost != null)
                return cost;

            if (!_cardPlayed) {
                _cardPlayed = true;
                _playCardEffect = new PlayEventEffect(this, _playedFrom, _eventPlayed, _requiresRanger);
                return _playCardEffect;
            }

            if (_playCardEffect != null && _playCardEffect.getPlayEventResult().isEventNotCancelled()) {
                Effect effect = getNextEffect();
                if (effect != null)
                    return effect;
            }
        }

        if (!_cardDiscarded && (_eventPlayed.getZone() == Zone.VOID || _eventPlayed.getZone() == Zone.VOID_FROM_HAND)) {
            _cardDiscarded = true;
            _game.getGameState().removeCardsFromZone(_eventPlayed.getOwnerName(), Collections.singleton(_eventPlayed));
            _game.getGameState().addCardToZone(_eventPlayed, Zone.DISCARD);
        }

        return null;
    }

    @Override
    public DefaultGame getGame() { return _game; }
}
