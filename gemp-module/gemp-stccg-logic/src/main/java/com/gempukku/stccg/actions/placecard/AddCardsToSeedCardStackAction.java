package com.gempukku.stccg.actions.placecard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.GameLocation;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.player.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AddCardsToSeedCardStackAction extends ActionyAction implements TopLevelSelectableAction {

    @JsonProperty("targetCardIds")
    @JsonIdentityReference(alwaysAsId=true)
    private final List<PhysicalCard> _cardsBeingSeeded = new ArrayList<>();
    @JsonProperty("locationId")
    private final int _locationId;

    private final PhysicalCard _performingCard; // mission or Empok Nor that cards are seeding under

    public AddCardsToSeedCardStackAction(DefaultGame cardGame, Player performingPlayer, MissionLocation location) {
        super(cardGame, performingPlayer, ActionType.ADD_CARDS_TO_PRESEED_STACK);
        _locationId = location.getLocationId();
        _performingCard = location.getTopMissionCard();
    }


    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    public void processEffect(DefaultGame cardGame) {
        try {
            GameState gameState = cardGame.getGameState();
            for (PhysicalCard seedCard : _cardsBeingSeeded) {
                gameState.removeCardsFromZoneWithoutSendingToClient(cardGame, Collections.singleton(seedCard));
                seedCard.setZone(Zone.VOID);
                if (gameState instanceof ST1EGameState stGameState) {
                    GameLocation location = stGameState.getLocationById(_locationId);
                    if (location instanceof MissionLocation missionLocation) {
                        missionLocation.addCardToTopOfPreSeedPile(seedCard, _performingPlayerId);
                    } else {
                        throw new InvalidGameLogicException("Unable to seed cards under a non-mission location");
                    }
                } else {
                    throw new InvalidGameLogicException("Unable to seed cards under a mission in a non-1E game");
                }
            }
            setAsSuccessful();
        } catch(InvalidGameLogicException exp) {
            cardGame.sendErrorMessage(exp);
            setAsFailed();
        }
    }

    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

    public void setSeedCards(Collection<PhysicalCard> seedCards) {
        _cardsBeingSeeded.addAll(seedCards);
    }

    public String getLocationName(ST1EGame cardGame) throws InvalidGameLogicException {
        GameLocation location = cardGame.getGameState().getLocationById(_locationId);
        if (location != null) {
            return location.getLocationName();
        } else {
            throw new InvalidGameLogicException("Unable to find location " + _locationId);
        }
    }

    public int getLocationId() { return _locationId; }

}