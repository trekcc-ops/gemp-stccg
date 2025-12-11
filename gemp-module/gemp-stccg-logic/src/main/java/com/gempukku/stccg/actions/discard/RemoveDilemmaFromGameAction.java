package com.gempukku.stccg.actions.discard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.FixedCardResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.gamestate.ST1EGameState;

import java.util.Collections;

public class RemoveDilemmaFromGameAction extends ActionyAction {
    private final FixedCardResolver _cardTarget;

    public RemoveDilemmaFromGameAction(DefaultGame cardGame, String performingPlayerName, PhysicalCard cardToRemove) {
        super(cardGame, performingPlayerName, ActionType.REMOVE_CARD_FROM_GAME);
        _cardTarget = new FixedCardResolver(cardToRemove);
        _cardTargets.add(_cardTarget);
    }


    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    protected void processEffect(DefaultGame cardGame) {
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
            cardToRemove.setLocationId(cardGame, -999);

            setAsSuccessful();
        } else {
            cardGame.sendErrorMessage("Tried to remove a dilemma from underneath a mission in a non-1E game");
            setAsFailed();
        }
    }

    @JsonProperty("targetCardId")
    @SuppressWarnings("unused")
    @JsonIdentityReference(alwaysAsId=true)
    private PhysicalCard getTargetCard() {
        return _cardTarget.getCard();
    }
}