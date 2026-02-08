package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.targetresolver.PlayFacilityResolver;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.List;

public class PlayFacilityAction extends PlayCardAction {
    private final PlayFacilityResolver _targetResolver;

    public PlayFacilityAction(DefaultGame cardGame, FacilityCard cardToSeed, PlayFacilityResolver targetResolver) {
        super(cardGame, cardToSeed, cardToSeed, cardToSeed.getOwnerName(), Zone.AT_LOCATION,
                ActionType.PLAY_CARD);
        _targetResolver = targetResolver;
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
                facility.setLocationId(stGame, destinationCard.getLocationId());
                stGame.getGameState().addCardToZone(stGame, facility, Zone.AT_LOCATION, _actionContext);
                saveResult(new PlayCardResult(this, _cardEnteringPlay), cardGame);
                setAsSuccessful();
            } else {
                cardGame.sendErrorMessage("Unable to process play outpost action in a non-1E game");
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