package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;

import java.util.List;

public class SeedCardAction extends PlayCardAction {

    public SeedCardAction(PhysicalCard cardToSeed) {
        this(cardToSeed, null);
    }

    public SeedCardAction(PhysicalCard cardToSeed, Zone zone) {
        super(cardToSeed, cardToSeed, cardToSeed.getOwner(), zone, ActionType.SEED_CARD);
        setText("Seed " + cardToSeed.getFullName());
    }

    @Override
    protected void putCardIntoPlay(DefaultGame game) {
        GameState gameState = game.getGameState();
        gameState.removeCardsFromZoneWithoutSendingToClient(game, List.of(_cardEnteringPlay));
        gameState.addCardToZoneWithoutSendingToClient(_cardEnteringPlay, _destinationZone);
        setAsSuccessful();
        game.getActionsEnvironment().emitEffectResult(new PlayCardResult(this, _cardEnteringPlay));
    }

}