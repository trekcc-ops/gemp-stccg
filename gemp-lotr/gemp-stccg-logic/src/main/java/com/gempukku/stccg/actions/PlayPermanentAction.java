package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Side;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effects.DiscountEffect;
import com.gempukku.stccg.effects.defaulteffect.PayTwilightCostEffect;
import com.gempukku.stccg.effects.defaulteffect.PlayCardEffect;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.effects.Effect;

import java.util.Collections;

public class PlayPermanentAction extends AbstractCostToEffectAction {
    private final PhysicalCard _permanentPlayed;
    private int _twilightModifier;
    private final boolean _ignoreRoamingPenalty;

    private boolean _cardRemoved;

    private PlayCardEffect _playCardEffect;
    private boolean _cardPlayed;

    private boolean _cardDiscarded;

    private boolean _discountResolved;
    private boolean _discountApplied;

    private boolean _skipShuffling;
    private final Zone _fromZone;
    private final Zone _toZone;
    private PhysicalCard _playedFromCard;
    private final DefaultGame _game;

    public PlayPermanentAction(PhysicalCard card, Zone zone, int twilightModifier, boolean ignoreRoamingPenalty) {
        _game = card.getGame();
        _permanentPlayed = card;
        setText("Play " + _permanentPlayed.getFullName());
        setPerformingPlayer(card.getOwnerName());
        _twilightModifier = twilightModifier;
        _ignoreRoamingPenalty = ignoreRoamingPenalty;

        if (card.getZone() == Zone.STACKED)
            _playedFromCard = card.getStackedOn();
        else if (card.getZone() == Zone.ATTACHED)
            _playedFromCard = card.getAttachedTo();

        _fromZone = card.getZone();
        _toZone = zone;
    }

    public void skipShufflingDeck() {
        _skipShuffling = true;
    }

    @Override
    public ActionType getActionType() {
        return ActionType.PLAY_CARD;
    }

    @Override
    public PhysicalCard getActionSource() {
        return _permanentPlayed;
    }

    @Override
    public PhysicalCard getActionAttachedToCard() {
        return _permanentPlayed;
    }

    @Override
    public Effect nextEffect() {
        if (!_cardRemoved) {
            _cardRemoved = true;
            final Zone playedFromZone = _permanentPlayed.getZone();
            _game.getGameState().sendMessage(_permanentPlayed.getOwnerName() + " plays " +
                    _permanentPlayed.getCardLink());
            _game.getGameState().removeCardsFromZone(_permanentPlayed.getOwnerName(), Collections.singleton(_permanentPlayed));
            if (playedFromZone == Zone.HAND)
                _game.getGameState().addCardToZone(_permanentPlayed, Zone.VOID_FROM_HAND);
            else
                _game.getGameState().addCardToZone(_permanentPlayed, Zone.VOID);
            if (playedFromZone == Zone.DRAW_DECK && !_skipShuffling) {
                _game.getGameState().sendMessage(_permanentPlayed.getOwnerName() + " shuffles their deck");
                _game.getGameState().shuffleDeck(_permanentPlayed.getOwnerName());
            }
        }

        if (!_discountResolved) {
            final DiscountEffect discount = getNextPotentialDiscount();
            if (discount != null) {
                if (_permanentPlayed.getBlueprint().getSide() == Side.SHADOW) {
                    int twilightCost = _game.getModifiersQuerying().getTwilightCost(_game, _permanentPlayed, null, _twilightModifier, _ignoreRoamingPenalty);
                    int requiredDiscount = Math.max(0, twilightCost - _game.getGameState().getTwilightPool() - getProcessedDiscount() - getPotentialDiscount());
                    discount.setMinimalRequiredDiscount(requiredDiscount);
                }
                return discount;
            } else {
                _discountResolved = true;
            }
        }

        if (!_discountApplied) {
            _discountApplied = true;
            _twilightModifier -= getProcessedDiscount();
            insertCost(new PayTwilightCostEffect(_game, _permanentPlayed, _twilightModifier, _ignoreRoamingPenalty));
        }

        if (!isCostFailed()) {
            Effect cost = getNextCost();
            if (cost != null)
                return cost;

            if (!_cardPlayed) {
                _cardPlayed = true;
                _playCardEffect = new PlayCardEffect(_game, _fromZone, _permanentPlayed, _toZone, _playedFromCard);
                return _playCardEffect;
            }

            return getNextEffect();
        } else {
            if (!_cardDiscarded) {
                _cardDiscarded = true;
                _game.getGameState().removeCardsFromZone(_permanentPlayed.getOwnerName(), Collections.singleton(_permanentPlayed));
                _game.getGameState().addCardToZone(_permanentPlayed, Zone.DISCARD);
            }
        }
        return null;
    }

    public boolean wasCarriedOut() {
        return _cardPlayed && _playCardEffect != null && _playCardEffect.wasCarriedOut();
    }

    @Override
    public DefaultGame getGame() { return _game; }
}
