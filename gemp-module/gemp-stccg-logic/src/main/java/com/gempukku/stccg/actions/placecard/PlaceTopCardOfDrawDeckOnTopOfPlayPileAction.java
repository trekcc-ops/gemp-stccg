package com.gempukku.stccg.actions.placecard;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

import java.util.Collections;

public class PlaceTopCardOfDrawDeckOnTopOfPlayPileAction extends ActionyAction {

    @JsonProperty("count")
    private final int _count;

    public PlaceTopCardOfDrawDeckOnTopOfPlayPileAction(Player performingPlayer, int count) {
        super(performingPlayer, ActionType.PLACE_CARD);
        _count = count;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return cardGame.getGameState().getDrawDeck(_performingPlayerId).size() >= _count;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) {
        int drawn = 0;

        while ((drawn < _count) && (!cardGame.getGameState().getDrawDeck(_performingPlayerId).isEmpty())) {
            PhysicalCard card = cardGame.getGameState().getDrawDeck(_performingPlayerId).getFirst();
            cardGame.getGameState().removeCardsFromZone(null, Collections.singleton(card));
            cardGame.getGameState().addCardToZone(card, Zone.PLAY_PILE);
            cardGame.sendMessage(card.getOwnerName() + " puts " + card.getCardLink() +
                    " from the top of their draw deck on top of their play pile");
            drawn++;
        }

        return getNextAction();
    }
    
}