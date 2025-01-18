package com.gempukku.stccg.actions.discard;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.FixedCardResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.MissionLocation;

import java.util.Collections;

public class RemoveDilemmaFromGameAction extends ActionyAction {
    private final FixedCardResolver _cardTarget;

    public RemoveDilemmaFromGameAction(Player performingPlayer, PhysicalCard cardToRemove) {
        super(performingPlayer, ActionType.REMOVE_CARD_FROM_PLAY);
        _cardTarget = new FixedCardResolver(cardToRemove);
    }


    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        if (cardGame instanceof ST1EGame stGame) {
            PhysicalCard cardToRemove = _cardTarget.getCard();

            for (MissionLocation mission : stGame.getGameState().getSpacelineLocations()) {
                if (mission.getCardsSeededUnderneath().contains(cardToRemove)) {
                    mission.removeSeedCard(cardToRemove);
                }
            }

            cardGame.getGameState().removeCardsFromZone(_performingPlayerId, Collections.singleton(cardToRemove));
            cardGame.getGameState().addCardToZone(cardToRemove, Zone.REMOVED);

            cardGame.sendMessage(_performingPlayerId + " removed " + cardToRemove.getCardLink() + " from the game");

            return getNextAction();
        } else {
            throw new InvalidGameLogicException("Tried to remove a dilemma from underneath a mission in a non-1E game");
        }
    }
}