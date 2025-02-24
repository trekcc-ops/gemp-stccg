package com.gempukku.stccg.actions.placecard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.gameevent.GameStateListener;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.modifiers.ModifierFlag;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class RemoveCardFromPreSeedStack extends ActionyAction {

    @JsonProperty("targetCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final PhysicalCard _targetCard;

    private final MissionLocation _location;

    public RemoveCardFromPreSeedStack(DefaultGame cardGame, Player performingPlayer, PhysicalCard cardRemoved,
                                      MissionLocation location) {
        super(cardGame, performingPlayer, ActionType.REMOVE_CARD_FROM_PRESEED_STACK);
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
        _location.removePreSeedCard(_targetCard, performingPlayer);
        cardGame.getGameState().removeCardsFromZoneWithoutSendingToClient(cardGame, Collections.singleton(_targetCard));
        List<PhysicalCard> zoneCards = performingPlayer.getCardGroupCards(Zone.SEED_DECK);
        zoneCards.add(_targetCard);
        _targetCard.setZone(Zone.SEED_DECK);
        setAsSuccessful();
    }
    
}