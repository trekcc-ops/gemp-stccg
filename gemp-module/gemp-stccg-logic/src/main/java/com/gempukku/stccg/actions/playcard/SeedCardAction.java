package com.gempukku.stccg.actions.playcard;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionStatus;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;

import java.util.List;

public class SeedCardAction extends PlayCardAction {

    @JsonCreator
    private SeedCardAction(@JsonProperty("actionId") int actionId,
                           @JsonProperty("seededCardId") @JsonIdentityReference(alwaysAsId=true)
                           PhysicalCard cardEnteringPlay,
                           @JsonProperty("performingCardId") @JsonIdentityReference(alwaysAsId=true)
                               PhysicalCard performingCard,
                           @JsonProperty("performingPlayerId")
                           String performingPlayerName,
                           @JsonProperty("destinationZone")
                           Zone destinationZone,
                           @JsonProperty("status")
                           ActionStatus status) {
        super(actionId, performingCard, cardEnteringPlay, performingPlayerName, destinationZone, ActionType.SEED_CARD,
                status);
    }

    public SeedCardAction(DefaultGame cardGame, PhysicalCard cardToSeed, Zone zone) {
        super(cardGame, cardToSeed, cardToSeed, cardToSeed.getOwnerName(), zone, ActionType.SEED_CARD);
    }

    public SeedCardAction(DefaultGame cardGame, PhysicalCard cardToSeed, Zone zone, ActionContext context) {
        super(cardGame, cardToSeed, cardToSeed, cardToSeed.getOwnerName(), zone, ActionType.SEED_CARD, context);
    }


    @Override
    protected void putCardIntoPlay(DefaultGame game) {
        GameState gameState = game.getGameState();
        game.removeCardsFromZone(List.of(_cardEnteringPlay));
        gameState.addCardToZone(game, _cardEnteringPlay, _destinationZone, _actionContext);
        setAsSuccessful();
        saveResult(new PlayCardResult(this, _cardEnteringPlay), game);
    }

    public void processEffect(DefaultGame cardGame) {
        putCardIntoPlay(cardGame);
        setAsSuccessful();
    }

    @JsonProperty("seededCardId")
    protected int getSeededCardId() {
        if (_cardEnteringPlay != null) {
            return _cardEnteringPlay.getCardId();
        } else {
            return 0;
        }
    }

}