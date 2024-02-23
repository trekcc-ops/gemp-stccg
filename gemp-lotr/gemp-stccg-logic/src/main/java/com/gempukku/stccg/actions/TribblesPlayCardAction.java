package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.cards.TribblesPhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.defaulteffect.TribblesPlayCardEffect;
import com.gempukku.stccg.game.TribblesGame;

import java.util.Collections;

public class TribblesPlayCardAction extends AbstractCostToEffectAction {
    private final TribblesPhysicalCard _cardToPlay;
    private boolean _cardRemoved;
    private Effect _playCardEffect;
    private boolean _cardPlayed;
    private final Zone _fromZone;
    private final Zone _toZone = Zone.PLAY_PILE;
    private final TribblesGame _game;

    public TribblesPlayCardAction(TribblesPhysicalCard card) {
        _cardToPlay = card;
        setText("Play " + _cardToPlay.getFullName());
        setPerformingPlayer(card.getOwnerName());

        _fromZone = card.getZone();
        _game = card.getGame();
    }

    @Override
    public boolean canBeInitiated() {
        if (!_cardToPlay.canBePlayed())
            return false;
        else return (_cardToPlay.isNextInSequence() || _cardToPlay.canPlayOutOfSequence());
    }

    @Override
    public TribblesGame getGame() { return _game; }

    @Override
    public ActionType getActionType() {
        return ActionType.PLAY_CARD;
    }

    @Override
    public PhysicalCard getActionSource() {
        return _cardToPlay;
    }

    @Override
    public PhysicalCard getActionAttachedToCard() {
        return _cardToPlay;
    }

    @Override
    public Effect nextEffect() {
        if (!_cardRemoved) {
            _cardRemoved = true;
            final Zone playedFromZone = _cardToPlay.getZone();
            _game.getGameState().sendMessage(_cardToPlay.getOwnerName() + " plays " +
                    _cardToPlay.getCardLink() +  " from " + playedFromZone.getHumanReadable() +
                    " to " + _toZone.getHumanReadable());
            _game.getGameState().removeCardsFromZone(_cardToPlay.getOwnerName(),
                    Collections.singleton(_cardToPlay));
            _game.getGameState().addCardToZone(_cardToPlay, Zone.PLAY_PILE);
/*            if (playedFromZone == Zone.HAND)
                _game.getGameState().addCardToZone(_game, _permanentPlayed, Zone.VOID_FROM_HAND);
            else
                _game.getGameState().addCardToZone(_game, _permanentPlayed, Zone.VOID); */
            if (playedFromZone == Zone.DRAW_DECK) {
                _game.getGameState().sendMessage(_cardToPlay.getOwnerName() + " shuffles their deck");
                _game.getGameState().shuffleDeck(_cardToPlay.getOwnerName());
            }
        }

        if (!_cardPlayed) {
            _cardPlayed = true;
            return new TribblesPlayCardEffect(this._game, _fromZone, _cardToPlay, _toZone);
        }

        return getNextEffect();
    }

    public boolean wasCarriedOut() {
        return _cardPlayed && _playCardEffect != null && _playCardEffect.wasCarriedOut();
    }
}
