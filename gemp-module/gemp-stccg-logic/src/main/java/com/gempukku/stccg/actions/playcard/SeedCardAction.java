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
        super(cardToSeed, cardToSeed, cardToSeed.getOwnerName(), zone, ActionType.SEED_CARD);
        setText("Seed " + cardToSeed.getFullName());
    }

    @Override
    protected void putCardIntoPlay(DefaultGame game) {

        GameState gameState = game.getGameState();

        game.sendMessage(_cardEnteringPlay.getOwnerName() + " seeded " + _cardEnteringPlay.getCardLink());
        gameState.removeCardFromZone(_cardEnteringPlay);
        if (_fromZone == Zone.DRAW_DECK) {
            game.sendMessage(_cardEnteringPlay.getOwnerName() + " shuffles their deck");
            gameState.shuffleDeck(_cardEnteringPlay.getOwnerName());
        }
        gameState.addCardToZone(_cardEnteringPlay, _toZone);
        game.getActionsEnvironment().emitEffectResult(new PlayCardResult(this, _fromZone, _cardEnteringPlay));
    }

}