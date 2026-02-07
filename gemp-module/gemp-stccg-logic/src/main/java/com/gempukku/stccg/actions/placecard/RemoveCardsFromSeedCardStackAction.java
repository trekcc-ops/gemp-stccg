package com.gempukku.stccg.actions.placecard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RemoveCardsFromSeedCardStackAction extends ActionyAction implements TopLevelSelectableAction {
    @JsonProperty("targetCardIds")
    @JsonIdentityReference(alwaysAsId=true)
    private final List<PhysicalCard> _cardsBeingRemoved = new ArrayList<>();
    @JsonProperty("locationId")
    @JsonIdentityReference(alwaysAsId=true)
    private final MissionLocation _location;
    private final PhysicalCard _performingCard;

    public RemoveCardsFromSeedCardStackAction(DefaultGame cardGame, Player performingPlayer,MissionLocation location) {
        super(cardGame, performingPlayer, ActionType.REMOVE_CARDS_FROM_PRESEED_STACK);
        _location = location;
        _performingCard = location.getTopMissionCard();
    }


    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    public void processEffect(DefaultGame cardGame) {
        try {
            Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
            for (PhysicalCard card : _cardsBeingRemoved) {
                _location.removePreSeedCard(card, _performingPlayerId);
                cardGame.getGameState().removeCardsFromZoneWithoutSendingToClient(cardGame, Collections.singleton(card));
                List<PhysicalCard> zoneCards = performingPlayer.getCardGroupCards(Zone.SEED_DECK);
                zoneCards.add(card);
                card.setZone(Zone.SEED_DECK);
            }
            setAsSuccessful();
        } catch(PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            setAsFailed();
        }
    }

    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

    public String getLocationName() {
        return _location.getLocationName();
    }

    public MissionLocation getLocation(){
        return _location;
    }

    public void setCardsToRemove(Collection<PhysicalCard> cards) {
        _cardsBeingRemoved.addAll(cards);
    }
}