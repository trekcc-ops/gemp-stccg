package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.TribblesPhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.TribblesGameState;

import java.util.Collections;

public class TribblesPlayCardAction extends PlayCardAction {
    private final TribblesPhysicalCard _cardToPlay;
    private boolean _cardRemoved;
    private boolean _cardPlayed;

    public TribblesPlayCardAction(TribblesPhysicalCard card) {
        super(card, card, card.getOwnerName(), Zone.PLAY_PILE, ActionType.PLAY_CARD);
        _cardToPlay = card;
        setText("Play " + _cardToPlay.getFullName());
    }

    @Override
    public boolean canBeInitiated(DefaultGame cardGame) {
        if (!_cardToPlay.canBePlayed(cardGame))
            return false;
        else return (_cardToPlay.isNextInSequence() || _cardToPlay.canPlayOutOfSequence());
    }

    @Override
    public PhysicalCard getCardForActionSelection() {
        return _cardToPlay;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) {
        TribblesGameState gameState = (TribblesGameState) cardGame.getGameState();

        if (!_cardRemoved) {
            _cardRemoved = true;
            final Zone playedFromZone = _cardToPlay.getZone();
            cardGame.sendMessage(_cardToPlay.getOwnerName() + " plays " +
                    _cardToPlay.getCardLink() +  " from " + playedFromZone.getHumanReadable() +
                    " to " + _toZone.getHumanReadable());
            gameState.removeCardsFromZone(_cardToPlay.getOwnerName(),
                    Collections.singleton(_cardToPlay));
            cardGame.getGameState().addCardToZone(_cardToPlay, Zone.PLAY_PILE);
            if (playedFromZone == Zone.DRAW_DECK) {
                cardGame.sendMessage(_cardToPlay.getOwnerName() + " shuffles their deck");
                gameState.shuffleDeck(_cardToPlay.getOwnerName());
            }
        }

        if (!_cardPlayed) {
            _cardPlayed = true;

            int tribbleValue = _cardEnteringPlay.getBlueprint().getTribbleValue();
            gameState.setLastTribblePlayed(tribbleValue);

            int nextTribble = (tribbleValue == 100000) ? 1 : (tribbleValue * 10);
            gameState.setNextTribbleInSequence(nextTribble);

            gameState.setChainBroken(false);
            cardGame.getActionsEnvironment().emitEffectResult(
                    new PlayCardResult(this, _fromZone, _cardEnteringPlay));
            return getNextAction();
        }

        return getNextAction();
    }
}