package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;

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

        Zone originalZone = _cardEnteringPlay.getZone();
        GameState gameState = game.getGameState();

        game.sendMessage(_cardEnteringPlay.getOwnerName() + " seeded " + _cardEnteringPlay.getCardLink());
        gameState.removeCardFromZone(_cardEnteringPlay);
        if (originalZone == Zone.DRAW_DECK) {
            game.sendMessage(_cardEnteringPlay.getOwnerName() + " shuffles their deck");
            _cardEnteringPlay.getOwner().shuffleDrawDeck(game);
        }
        gameState.addCardToZone(_cardEnteringPlay, _destinationZone);
        game.getActionsEnvironment().emitEffectResult(new PlayCardResult(this, originalZone, _cardEnteringPlay));
    }

}