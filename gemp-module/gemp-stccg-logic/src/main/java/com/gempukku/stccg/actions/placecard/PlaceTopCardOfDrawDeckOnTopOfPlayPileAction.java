package com.gempukku.stccg.actions.placecard;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.PlayerNotFoundException;

import java.util.Collections;

public class PlaceTopCardOfDrawDeckOnTopOfPlayPileAction extends ActionyAction {

    @JsonProperty("count")
    private final int _count;

    public PlaceTopCardOfDrawDeckOnTopOfPlayPileAction(DefaultGame cardGame, Player performingPlayer, int count) {
        super(cardGame, performingPlayer, ActionType.PLACE_CARD);
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
    public Action nextAction(DefaultGame cardGame) throws PlayerNotFoundException {
        int drawn = 0;
        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);

        while ((drawn < _count) && (!performingPlayer.getCardsInDrawDeck().isEmpty())) {
            PhysicalCard card = performingPlayer.getCardsInDrawDeck().getFirst();
            cardGame.removeCardsFromZone(null, Collections.singleton(card));
            cardGame.getGameState().addCardToZone(card, Zone.PLAY_PILE);
            cardGame.sendMessage(card.getOwnerName() + " puts " + card.getCardLink() +
                    " from the top of their draw deck on top of their play pile");
            drawn++;
        }

        setAsSuccessful();
        return getNextAction();
    }
    
}