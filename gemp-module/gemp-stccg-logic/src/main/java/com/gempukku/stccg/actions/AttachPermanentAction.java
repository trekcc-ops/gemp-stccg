package com.gempukku.stccg.actions;

import com.gempukku.stccg.actions.choose.ChooseActiveCardEffect;
import com.gempukku.stccg.actions.playcard.PlayCardEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;

import java.util.Collection;
import java.util.Collections;

public class AttachPermanentAction extends ActionyAction {
    private final PhysicalCard _cardToAttach;
    private boolean _cardRemoved;
    private boolean _targetChosen;
    private boolean _cardPlayed;
    private boolean _cardDiscarded;
    private final Zone _playedFrom;
    private PhysicalCard _target;
    private final Collection<PhysicalCard> _targetOptions;

    public AttachPermanentAction(final PhysicalCard card, Filter filter) {
        super(card.getOwner(), ActionType.PLAY_CARD);
        _cardToAttach = card;
        setText("Play " + _cardToAttach.getFullName());
        _playedFrom = card.getZone();
        _targetOptions = Filters.filterActive(card.getGame(), filter);
    }

    @Override
    public PhysicalCard getActionSource() {
        return _cardToAttach;
    }

    @Override
    public PhysicalCard getCardForActionSelection() {
        return _cardToAttach;
    }

    public boolean requirementsAreMet(DefaultGame game) { return true; }

    @Override
    public Action nextAction(DefaultGame cardGame) {
        GameState gameState = cardGame.getGameState();
        if (!_cardRemoved) {
            _cardRemoved = true;
            final Zone playedFromZone = _cardToAttach.getZone();
            gameState
                    .removeCardsFromZone(_cardToAttach.getOwnerName(), Collections.singleton(_cardToAttach));
            if (playedFromZone == Zone.HAND)
                gameState.addCardToZone(_cardToAttach, Zone.VOID_FROM_HAND);
            else
                gameState.addCardToZone(_cardToAttach, Zone.VOID);
            if (playedFromZone == Zone.DRAW_DECK) {
                cardGame.sendMessage(_cardToAttach.getOwnerName() + " shuffles their deck");
                gameState.shuffleDeck(_cardToAttach.getOwnerName());
            }
        }

        if (!_targetChosen) {
            _targetChosen = true;
            Effect chooseTargetEffect =
                    new ChooseActiveCardEffect(null, _cardToAttach.getOwnerName(), "Attach " +
                            _cardToAttach.getFullName() + ". Choose target to attach to", _targetOptions) {
                        @Override
                        protected void cardSelected(PhysicalCard target) {
                            _target = target;
                            _game.sendMessage(_cardToAttach.getOwnerName() + " plays " + _cardToAttach.getCardLink() +
                                    " from " + _playedFrom.getHumanReadable() + " on " + target.getCardLink());
                        }
                    };
            return new SubAction(this,chooseTargetEffect);
        }

        if ((_target != null) && (!isCostFailed())) {
            Action cost = getNextCost();
            if (cost != null)
                return cost;

            if (!_cardPlayed) {
                _cardPlayed = true;
                return new SubAction(this, new PlayCardEffect(
                        _performingPlayerId, _playedFrom, _cardToAttach, _target, null));
            }

            return getNextAction();
        } else {
            if (!_cardDiscarded) {
                _cardDiscarded = true;
                gameState.removeCardsFromZone(_cardToAttach.getOwnerName(), Collections.singleton(_cardToAttach));
                gameState.addCardToZone(_cardToAttach, Zone.DISCARD);
            }
        }

        return null;
    }

}