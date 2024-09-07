package com.gempukku.stccg.actions.lotr;

import com.gempukku.stccg.actions.AbstractCostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.choose.ChooseActiveCardEffect;
import com.gempukku.stccg.actions.discard.DiscountEffect;
import com.gempukku.stccg.actions.playcard.PlayCardEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.common.filterable.lotr.Side;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collections;

public class AttachPermanentAction extends AbstractCostToEffectAction {
    private final PhysicalCard _cardToAttach;

    private boolean _cardRemoved;

    private final ChooseActiveCardEffect _chooseTargetEffect;
    private boolean _targetChosen;

    private boolean _cardPlayed;

    private boolean _cardDiscarded;

    private boolean _discountResolved;
    private boolean _discountApplied;

    private int _twilightModifier;
    private final Zone _playedFrom;
    private PhysicalCard _target;
    private final DefaultGame _game;

    public AttachPermanentAction(final PhysicalCard card, Filter filter, final int twilightModifier) {
        super(card.getOwner(), ActionType.PLAY_CARD);
        _game = card.getGame();
        _cardToAttach = card;
        setText("Play " + _cardToAttach.getFullName());
        _playedFrom = card.getZone();
        _twilightModifier = twilightModifier;

        _chooseTargetEffect =
                new ChooseActiveCardEffect(null, card.getOwnerName(), "Attach " + card.getFullName() + ". Choose target to attach to", filter) {
                    @Override
                    protected void cardSelected(PhysicalCard target) {
                        _target = target;
                        _game.sendMessage(card.getOwnerName() + " plays " + card.getCardLink() +
                                " from " + _playedFrom.getHumanReadable() + " on " + target.getCardLink());
                    }
                };
    }
    public PhysicalCard getTarget() {
        return _target;
    }

    @Override
    public PhysicalCard getActionSource() {
        return _cardToAttach;
    }

    @Override
    public PhysicalCard getActionAttachedToCard() {
        return _cardToAttach;
    }

    @Override
    public Effect nextEffect() {
        if (!_cardRemoved) {
            _cardRemoved = true;
            final Zone playedFromZone = _cardToAttach.getZone();
            _game.getGameState().removeCardsFromZone(_cardToAttach.getOwnerName(), Collections.singleton(_cardToAttach));
            if (playedFromZone == Zone.HAND)
                _game.getGameState().addCardToZone(_cardToAttach, Zone.VOID_FROM_HAND);
            else
                _game.getGameState().addCardToZone(_cardToAttach, Zone.VOID);
            if (playedFromZone == Zone.DRAW_DECK) {
                _game.sendMessage(_cardToAttach.getOwnerName() + " shuffles their deck");
                _game.getGameState().shuffleDeck(_cardToAttach.getOwnerName());
            }
        }

        if (!_targetChosen) {
            _targetChosen = true;
            return _chooseTargetEffect;
        }

        if (!_discountResolved) {
            final DiscountEffect discount = getNextPotentialDiscount();
            if (discount != null) {
                if (_cardToAttach.getBlueprint().getSide() == Side.SHADOW) {
                    int twilightCost = _game.getModifiersQuerying().getTwilightCost(_game, _cardToAttach, _target, _twilightModifier, false);
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
            insertCost(new PayPlayOnTwilightCostEffect(_game, _cardToAttach, _target, _twilightModifier));
        }

        if ((_target != null) && (!isCostFailed())) {
            Effect cost = getNextCost();
            if (cost != null)
                return cost;

            if (!_cardPlayed) {
                _cardPlayed = true;

                return new PlayCardEffect(_performingPlayerId, _playedFrom, _cardToAttach, _target, null);
            }

            return getNextEffect();
        } else {
            if (!_cardDiscarded) {
                _cardDiscarded = true;
                _game.getGameState().removeCardsFromZone(_cardToAttach.getOwnerName(), Collections.singleton(_cardToAttach));
                _game.getGameState().addCardToZone(_cardToAttach, Zone.DISCARD);
            }
        }

        return null;
    }

    @Override
    public DefaultGame getGame() { return _game; }
}
