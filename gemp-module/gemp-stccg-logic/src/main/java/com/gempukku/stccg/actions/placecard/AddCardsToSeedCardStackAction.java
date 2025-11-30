package com.gempukku.stccg.actions.placecard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AddCardsToSeedCardStackAction extends ActionyAction implements TopLevelSelectableAction {

    @JsonProperty("targetCardIds")
    @JsonIdentityReference(alwaysAsId=true)
    private final List<PhysicalCard> _cardsBeingSeeded = new ArrayList<>();
    @JsonProperty("locationId")
    @JsonIdentityReference(alwaysAsId=true)
    private final MissionLocation _location;

    private final PhysicalCard _performingCard; // mission or Empok Nor that cards are seeding under

    public AddCardsToSeedCardStackAction(DefaultGame cardGame, Player performingPlayer, MissionLocation location) {
        super(cardGame, performingPlayer, ActionType.ADD_CARDS_TO_PRESEED_STACK);
        _location = location;
        _performingCard = location.getTopMissionCard();
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
        GameState gameState = cardGame.getGameState();
        for (PhysicalCard seedCard : _cardsBeingSeeded) {
            gameState.removeCardsFromZoneWithoutSendingToClient(cardGame, Collections.singleton(seedCard));
            seedCard.setZone(Zone.VOID);
            _location.addCardToTopOfPreSeedPile(seedCard, performingPlayer);
        }
        setAsSuccessful();
    }

    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

    public void setSeedCards(Collection<PhysicalCard> seedCards) {
        _cardsBeingSeeded.addAll(seedCards);
    }

    public String getLocationName() {
        return _location.getLocationName();
    }

    public MissionLocation getLocation() { return _location; }
}