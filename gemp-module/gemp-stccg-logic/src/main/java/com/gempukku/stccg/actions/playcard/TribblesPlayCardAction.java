package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.cards.physicalcard.TribblesPhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.TribblesGame;
import com.gempukku.stccg.gamestate.TribblesGameState;

import java.util.Collections;

public class TribblesPlayCardAction extends PlayCardAction {

    public TribblesPlayCardAction(DefaultGame cardGame, TribblesPhysicalCard card) {
        super(cardGame, card, card, card.getOwnerName(), Zone.PLAY_PILE, ActionType.PLAY_CARD);
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        TribblesGame tribblesGame = (TribblesGame) cardGame;
        if (_cardEnteringPlay instanceof TribblesPhysicalCard tribblesCard) {
            if (!tribblesCard.canBePlayed(tribblesGame))
                return false;
            else return (tribblesCard.isNextInSequence(tribblesGame) || tribblesCard.canPlayOutOfSequence(tribblesGame));
        } else {
            return false;
        }
    }

    protected void processEffect(DefaultGame cardGame) {
        TribblesGameState gameState = (TribblesGameState) cardGame.getGameState();

        gameState.removeCardsFromZoneWithoutSendingToClient(cardGame, Collections.singleton(_cardEnteringPlay));
        gameState.addCardToZone(cardGame, _cardEnteringPlay, Zone.PLAY_PILE, _actionContext);

        int tribbleValue = _cardEnteringPlay.getBlueprint().getTribbleValue();
        gameState.setLastTribblePlayed(tribbleValue);

        int nextTribble = (tribbleValue == 100000) ? 1 : (tribbleValue * 10);
        gameState.setNextTribbleInSequence(nextTribble);

        gameState.setChainBroken(false);
        saveResult(new PlayCardResult(this, _cardEnteringPlay), cardGame);
        setAsSuccessful();
    }
}