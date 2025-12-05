package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;

import java.util.List;

public class SeedCardAction extends PlayCardAction {

    public SeedCardAction(DefaultGame cardGame, PhysicalCard cardToSeed, Zone zone) {
        super(cardGame, cardToSeed, cardToSeed, cardToSeed.getOwnerName(), zone, ActionType.SEED_CARD);
    }

    public SeedCardAction(DefaultGame cardGame, PhysicalCard cardToSeed, Zone zone, ActionContext context) {
        super(cardGame, cardToSeed, cardToSeed, cardToSeed.getOwnerName(), zone, ActionType.SEED_CARD, context);
    }
    public SeedCardAction(DefaultGame cardGame, PhysicalCard cardToSeed, ActionContext context) {
        this(cardGame, cardToSeed, null, context);
    }


    @Override
    protected void putCardIntoPlay(DefaultGame game) {
        GameState gameState = game.getGameState();
        gameState.removeCardsFromZoneWithoutSendingToClient(game, List.of(_cardEnteringPlay));
        gameState.addCardToZone(game, _cardEnteringPlay, _destinationZone, _actionContext);
        setAsSuccessful();
        saveResult(new PlayCardResult(this, _cardEnteringPlay));
    }

    public void processEffect(DefaultGame cardGame) {
        putCardIntoPlay(cardGame);
    }

}