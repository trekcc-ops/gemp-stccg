package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.targetresolver.SeedOutpostResolver;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ChildCardRelationshipType;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.List;
import java.util.Map;

public class SeedFacilityAction extends SeedCardAction {
    private final SeedOutpostResolver _targetResolver;

    public SeedFacilityAction(DefaultGame cardGame, FacilityCard cardToSeed,
                              Map<PhysicalCard, List<Affiliation>> destinationMap) {
        super(cardGame, cardToSeed, Zone.AT_LOCATION);
        _targetResolver = new SeedOutpostResolver(cardToSeed, destinationMap);
        _cardTargets.add(_targetResolver);
    }


    public void processEffect(DefaultGame cardGame) {
        try {
            if (cardGame instanceof ST1EGame stGame) {
                Player performingPlayer = stGame.getPlayer(_performingPlayerId);

                Affiliation selectedAffiliation = _targetResolver.getAffiliationToPlayAs();
                FacilityCard facility = (FacilityCard) _cardEnteringPlay;
                facility.changeAffiliation(stGame, selectedAffiliation);

                stGame.getGameState().removeCardsFromZoneWithoutSendingToClient(stGame, List.of(_cardEnteringPlay));
                performingPlayer.addPlayedAffiliation(selectedAffiliation);
                MissionCard destinationCard = _targetResolver.getDestinationMission();
                stGame.getGameState().addCardToZone(stGame, facility, Zone.AT_LOCATION, _actionContext);
                facility.setParentCardRelationship(destinationCard, ChildCardRelationshipType.IN_SPACE);
                saveResult(new PlayCardResult(this, _cardEnteringPlay), cardGame);
                setAsSuccessful();
            } else {
                cardGame.sendErrorMessage("Unable to process seed outpost action in a non-1E game");
                setAsFailed();
            }
        } catch(PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            setAsFailed();
        }
    }

    public void setDestination(MissionCard mission) {
        _targetResolver.setDestination(mission);
    }

    public void setAffiliation(Affiliation affiliation) {
        _targetResolver.setAffiliation(affiliation);
    }

}