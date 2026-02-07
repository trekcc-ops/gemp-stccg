package com.gempukku.stccg.actions.placecard;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.List;

public class PlaceTopCardOfDrawDeckOnTopOfPlayPileAction extends ActionyAction {

    @JsonProperty("count")
    private final int _count;

    public PlaceTopCardOfDrawDeckOnTopOfPlayPileAction(DefaultGame cardGame, Player performingPlayer, int count) {
        super(cardGame, performingPlayer, ActionType.PLACE_CARD_IN_PLAY_PILE);
        _count = count;
    }


    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        try {
            Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
            return performingPlayer.getCardsInDrawDeck().size() >= _count;
        } catch(PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            return false;
        }
    }

    @Override
    protected void processEffect(DefaultGame cardGame) {
        try {
            int drawn = 0;
            Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
            while ((drawn < _count) && (!performingPlayer.getCardsInDrawDeck().isEmpty())) {
                PhysicalCard card = performingPlayer.getCardsInDrawDeck().getFirst();
                cardGame.getGameState().removeCardsFromZoneWithoutSendingToClient(cardGame, List.of(card));
                cardGame.getGameState().addCardToZone(cardGame, card, Zone.PLAY_PILE, _actionContext);
                drawn++;
            }
            setAsSuccessful();
        } catch(PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            setAsFailed();
        }
    }
    
}