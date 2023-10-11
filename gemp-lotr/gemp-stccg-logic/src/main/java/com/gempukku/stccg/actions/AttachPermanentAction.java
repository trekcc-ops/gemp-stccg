package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.Side;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effects.*;
import com.gempukku.stccg.effects.choose.ChooseActiveCardEffect;
import com.gempukku.stccg.effects.DiscountEffect;
import com.gempukku.stccg.effects.defaulteffect.PayPlayOnTwilightCostEffect;
import com.gempukku.stccg.effects.defaulteffect.PlayCardEffect;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.GameUtils;

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

    public AttachPermanentAction(DefaultGame game, final PhysicalCard card, Filter filter, final int twilightModifier) {
        _game = game;
        _cardToAttach = card;
        setText("Play " + GameUtils.getFullName(_cardToAttach));
        _playedFrom = card.getZone();
        _twilightModifier = twilightModifier;

        _chooseTargetEffect =
                new ChooseActiveCardEffect(_game,null, card.getOwner(), "Attach " + GameUtils.getFullName(card) + ". Choose target to attach to", filter) {
                    @Override
                    protected void cardSelected(DefaultGame game, PhysicalCard target) {
                        _target = target;
                        _game.getGameState().sendMessage(card.getOwner() + " plays " + GameUtils.getCardLink(card) + " from " + _playedFrom.getHumanReadable() + " on " + GameUtils.getCardLink(target));
                    }
                };
    }


    @Override
    public ActionType getActionType() {
        return ActionType.PLAY_CARD;
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
    public Effect nextEffect(DefaultGame game) {
        if (!_cardRemoved) {
            _cardRemoved = true;
            final Zone playedFromZone = _cardToAttach.getZone();
            game.getGameState().removeCardsFromZone(_cardToAttach.getOwner(), Collections.singleton(_cardToAttach));
            if (playedFromZone == Zone.HAND)
                game.getGameState().addCardToZone(game, _cardToAttach, Zone.VOID_FROM_HAND);
            else
                game.getGameState().addCardToZone(game, _cardToAttach, Zone.VOID);
            if (playedFromZone == Zone.DRAW_DECK) {
                game.getGameState().sendMessage(_cardToAttach.getOwner() + " shuffles their deck");
                game.getGameState().shuffleDeck(_cardToAttach.getOwner());
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
                    int twilightCost = game.getModifiersQuerying().getTwilightCost(game, _cardToAttach, _target, _twilightModifier, false);
                    int requiredDiscount = Math.max(0, twilightCost - game.getGameState().getTwilightPool() - getProcessedDiscount() - getPotentialDiscount(game));
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
            insertCost(new PayPlayOnTwilightCostEffect(game, _cardToAttach, _target, _twilightModifier));
        }

        if ((_target != null) && (!isCostFailed())) {
            Effect cost = getNextCost();
            if (cost != null)
                return cost;

            if (!_cardPlayed) {
                _cardPlayed = true;

                return new PlayCardEffect(_game, _playedFrom, _cardToAttach, _target, null);
            }

            return getNextEffect();
        } else {
            if (!_cardDiscarded) {
                _cardDiscarded = true;
                game.getGameState().removeCardsFromZone(_cardToAttach.getOwner(), Collections.singleton(_cardToAttach));
                game.getGameState().addCardToZone(game, _cardToAttach, Zone.DISCARD);
            }
        }

        return null;
    }
}
