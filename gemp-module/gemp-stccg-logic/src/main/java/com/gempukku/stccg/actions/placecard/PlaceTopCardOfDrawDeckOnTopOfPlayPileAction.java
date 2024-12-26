package com.gempukku.stccg.actions.placecard;

import com.gempukku.stccg.TextUtils;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.Preventable;

import java.util.Collections;

public class PlaceTopCardOfDrawDeckOnTopOfPlayPileAction extends ActionyAction {
    private final int _count;
    private final PhysicalCard _performingCard;

    public PlaceTopCardOfDrawDeckOnTopOfPlayPileAction(Player performingPlayer, PhysicalCard performingCard, int count) {
        super(performingPlayer, ActionType.PLACE_CARD);
        _count = count;
        _performingCard = performingCard;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return cardGame.getGameState().getDrawDeck(_performingPlayerId).size() >= _count;
    }
    
    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

    @Override
    public PhysicalCard getCardForActionSelection() {
        return _performingCard;
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