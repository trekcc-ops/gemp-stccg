package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.cards.physicalcard.TribblesPhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.gamestate.TribblesGameState;

import java.util.Collections;

public class TribblesPlayCardAction extends PlayCardAction {

    public TribblesPlayCardAction(TribblesPhysicalCard card) {
        super(card, card, card.getOwner(), Zone.PLAY_PILE, ActionType.PLAY_CARD);
        setText("Play " + card.getFullName());
    }

    @Override
    public boolean canBeInitiated(DefaultGame cardGame) {
        TribblesGame tribblesGame = (TribblesGame) cardGame;
        if (_cardEnteringPlay instanceof TribblesPhysicalCard tribblesCard) {
            if (!tribblesCard.canBePlayed(tribblesGame))
                return false;
            else return (tribblesCard.isNextInSequence(tribblesGame) || tribblesCard.canPlayOutOfSequence(tribblesGame));
        } else {
            return false;
        }
    }

    @Override
    public Action nextAction(DefaultGame cardGame) {
        TribblesGameState gameState = (TribblesGameState) cardGame.getGameState();

        gameState.removeCardsFromZoneWithoutSendingToClient(cardGame, Collections.singleton(_cardEnteringPlay));
        gameState.addCardToZoneWithoutSendingToClient(_cardEnteringPlay, Zone.PLAY_PILE);

        int tribbleValue = _cardEnteringPlay.getBlueprint().getTribbleValue();
        gameState.setLastTribblePlayed(tribbleValue);

        int nextTribble = (tribbleValue == 100000) ? 1 : (tribbleValue * 10);
        gameState.setNextTribbleInSequence(nextTribble);

        gameState.setChainBroken(false);
        saveResult(new PlayCardResult(this, _cardEnteringPlay));
        _wasCarriedOut = true;
        setAsSuccessful();
        return null;
    }
}