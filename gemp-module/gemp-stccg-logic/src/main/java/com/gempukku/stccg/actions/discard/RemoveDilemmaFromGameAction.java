package com.gempukku.stccg.actions.discard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.FixedCardResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.gamestate.NullLocation;
import com.gempukku.stccg.gamestate.ST1EGameState;

import java.util.Collections;

public class RemoveDilemmaFromGameAction extends ActionyAction {
    private final FixedCardResolver _cardTarget;

    public RemoveDilemmaFromGameAction(Player performingPlayer, PhysicalCard cardToRemove) {
        super(cardToRemove.getGame(), performingPlayer, ActionType.REMOVE_CARD_FROM_GAME);
        _cardTarget = new FixedCardResolver(cardToRemove);
    }


    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        if (cardGame instanceof ST1EGame stGame) {
            ST1EGameState gameState = stGame.getGameState();
            PhysicalCard cardToRemove = _cardTarget.getCard();

            for (MissionLocation mission : gameState.getSpacelineLocations()) {
                if (mission.getSeedCards().contains(cardToRemove)) {
                    mission.removeSeedCard(cardToRemove);
                }
            }

            gameState.removeCardsFromZone(cardGame, _performingPlayerId, Collections.singleton(cardToRemove));
            gameState.addCardToZone(cardToRemove, Zone.REMOVED);
            cardToRemove.setLocation(new NullLocation());

            cardGame.sendMessage(_performingPlayerId + " removed " + cardToRemove.getCardLink() + " from the game");
            setAsSuccessful();
            return getNextAction();
        } else {
            setAsFailed();
            throw new InvalidGameLogicException("Tried to remove a dilemma from underneath a mission in a non-1E game");
        }
    }
}