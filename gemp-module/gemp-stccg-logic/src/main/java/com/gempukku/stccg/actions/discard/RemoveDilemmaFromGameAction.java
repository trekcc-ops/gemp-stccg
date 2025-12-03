package com.gempukku.stccg.actions.discard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.FixedCardResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.gamestate.NullLocation;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.Collections;

public class RemoveDilemmaFromGameAction extends ActionyAction {
    private final FixedCardResolver _cardTarget;

    public RemoveDilemmaFromGameAction(DefaultGame cardGame, String performingPlayerName, PhysicalCard cardToRemove) {
        super(cardGame, performingPlayerName, ActionType.REMOVE_CARD_FROM_GAME);
        _cardTarget = new FixedCardResolver(cardToRemove);
    }


    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        if (cardGame instanceof ST1EGame stGame) {
            ST1EGameState gameState = stGame.getGameState();
            PhysicalCard cardToRemove = _cardTarget.getCard();

            for (MissionLocation mission : gameState.getSpacelineLocations()) {
                if (mission.getSeedCards().contains(cardToRemove)) {
                    mission.removeSeedCard(cardToRemove);
                }
            }

            gameState.removeCardsFromZoneWithoutSendingToClient(cardGame, Collections.singleton(cardToRemove));
            gameState.addCardToRemovedPile(cardToRemove);
            cardToRemove.setLocation(cardGame, new NullLocation());

            setAsSuccessful();
            return getNextAction();
        } else {
            setAsFailed();
            throw new InvalidGameLogicException("Tried to remove a dilemma from underneath a mission in a non-1E game");
        }
    }

    @JsonProperty("targetCardId")
    @SuppressWarnings("unused")
    @JsonIdentityReference(alwaysAsId=true)
    private PhysicalCard getTargetCard() {
        return _cardTarget.getCard();
    }
}