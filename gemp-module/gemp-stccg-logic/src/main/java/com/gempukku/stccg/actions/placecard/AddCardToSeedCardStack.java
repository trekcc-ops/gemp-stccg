package com.gempukku.stccg.actions.placecard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.Collections;

public class AddCardToSeedCardStack extends ActionyAction {

    @JsonProperty("targetCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final PhysicalCard _targetCard;

    private final MissionLocation _location;

    public AddCardToSeedCardStack(DefaultGame cardGame, Player performingPlayer, PhysicalCard cardRemoved,
                                  MissionLocation location) {
        super(cardGame, performingPlayer, ActionType.ADD_CARD_TO_PRESEED_STACK);
        _targetCard = cardRemoved;
        _location = location;
    }


    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws PlayerNotFoundException {
        Action nextAction = getNextAction();
        if (nextAction == null) {
            processEffect(cardGame.getPlayer(_performingPlayerId), cardGame);
        }
        return nextAction;
    }

    public void processEffect(Player performingPlayer, DefaultGame cardGame) {
        GameState gameState = cardGame.getGameState();
        gameState.removeCardsFromZoneWithoutSendingToClient(cardGame, Collections.singleton(_targetCard));
        _targetCard.setZone(Zone.VOID);
        setAsSuccessful();
        _location.addCardToTopOfPreSeedPile(_targetCard, performingPlayer);
    }
    
}