package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.TribblesPhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.game.TribblesGame;

import java.util.Collections;

public class TribblesPlayCardAction extends PlayCardAction {
    private final TribblesPhysicalCard _cardToPlay;
    private boolean _cardRemoved;
    private boolean _cardPlayed;
    private final Zone _toZone = Zone.PLAY_PILE;
    private final TribblesGame _game;

    public TribblesPlayCardAction(TribblesPhysicalCard card) {
        super(card, card, card.getOwnerName(), Zone.PLAY_PILE, ActionType.PLAY_CARD);
        _cardToPlay = card;
        setText("Play " + _cardToPlay.getFullName());
        Zone _fromZone = card.getZone();
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
    public PhysicalCard getActionAttachedToCard() {
        return _cardToPlay;
    }

    @Override
    protected Effect getFinalEffect() {
        return new TribblesPlayCardEffect(_cardToPlay, _toZone);
    }

    @Override
    public Effect nextEffect() {
        if (!_cardRemoved) {
            _cardRemoved = true;
            final Zone playedFromZone = _cardToPlay.getZone();
            _game.sendMessage(_cardToPlay.getOwnerName() + " plays " +
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
                _game.sendMessage(_cardToPlay.getOwnerName() + " shuffles their deck");
                _game.getGameState().shuffleDeck(_cardToPlay.getOwnerName());
            }
        }

        if (!_cardPlayed) {
            _cardPlayed = true;
            _finalEffect = getFinalEffect();
            return _finalEffect;
        }

        return getNextEffect();
    }
}
