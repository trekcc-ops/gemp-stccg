package com.gempukku.stccg.actions.playcard;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.targetresolver.SeedMissionTargetResolver;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.common.filterable.Quadrant;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.gamestate.ST1EGameState;

import java.util.List;
import java.util.Objects;

public class SeedMissionCardAction extends PlayCardAction {

    @JsonProperty("locationZoneIndex")
    private int _locationZoneIndex;
    private final SeedMissionTargetResolver _resolver;

    @JsonCreator
    @SuppressWarnings("unused")
    private SeedMissionCardAction(@JsonProperty("actionId") int actionId,
                           @JsonProperty("targetCardId") @JsonIdentityReference(alwaysAsId=true)
                           MissionCard cardEnteringPlay,
                          @JsonProperty("performingCardId") @JsonIdentityReference(alwaysAsId=true)
                          MissionCard performingCard,
                           @JsonProperty("performingPlayerId") String performingPlayerName,
                           @JsonProperty("destinationZone") Zone destinationZone,
                                  @JsonProperty("locationZoneIndex") Integer locationZoneIndex) {
        super(actionId, performingCard, cardEnteringPlay, performingPlayerName, destinationZone, ActionType.SEED_CARD);
        _locationZoneIndex = locationZoneIndex;
        _resolver = new SeedMissionTargetResolver(cardEnteringPlay, locationZoneIndex);
        _cardTargets.add(_resolver);
    }


    public SeedMissionCardAction(DefaultGame cardGame, MissionCard cardToPlay) {
        super(cardGame, cardToPlay, cardToPlay, cardToPlay.getOwnerName(), Zone.SPACELINE, ActionType.SEED_CARD);
        _resolver = new SeedMissionTargetResolver(cardToPlay);
        _cardTargets.add(_resolver);
    }

    public SeedMissionCardAction(ST1EGame cardGame, MissionCard cardToPlay, int locationZoneIndex) {
        super(cardGame, cardToPlay, cardToPlay, cardToPlay.getOwnerName(), Zone.SPACELINE, ActionType.SEED_CARD);
        _locationZoneIndex = locationZoneIndex;
        _resolver = new SeedMissionTargetResolver(cardToPlay, locationZoneIndex);
        _cardTargets.add(_resolver);
    }

    public void processEffect(DefaultGame game) {
        _locationZoneIndex = _resolver.getLocationZoneIndex();
        Quadrant quadrant = _cardEnteringPlay.getBlueprint().getQuadrant();
        String missionLocationName = _cardEnteringPlay.getBlueprint().getLocation();

        if (game.getGameState() instanceof ST1EGameState gameState && _cardEnteringPlay instanceof MissionCard mission) {
            boolean sharedMission = gameState.indexOfLocation(missionLocationName, quadrant) != null &&
                    !_cardEnteringPlay.getBlueprint().isUniversal();

            gameState.removeCardsFromZoneWithoutSendingToClient(game, List.of(_cardEnteringPlay));
            List<MissionLocation> spaceline = gameState.getSpacelineLocations();

            try {
                if (sharedMission) {
                    MissionLocation location = spaceline.get(_locationZoneIndex);
                    List<MissionCard> missionsAtLocation = location.getMissionCards();
                    if (missionsAtLocation.size() != 1 ||
                            Objects.equals(missionsAtLocation.getFirst().getOwnerName(), mission.getOwnerName()))
                        throw new InvalidGameLogicException("Cannot seed " + mission.getTitle() + " because " +
                                mission.getOwnerName() + " already has a mission at " +
                                mission.getBlueprint().getLocation());
                    location.addMission(game, mission);
                    gameState.addCardToZone(game, mission, Zone.SPACELINE, _actionContext);
                }
                else {
                    int newLocationId = gameState.getNextLocationId();
                    MissionLocation location = new MissionLocation(game, mission, newLocationId);
                    gameState.addSpacelineLocation(_locationZoneIndex, location);
                    gameState.addCardToZone(game, mission, Zone.SPACELINE, _actionContext);
                }
                saveResult(new PlayCardResult(this, _cardEnteringPlay), game);
                setAsSuccessful();
            } catch (InvalidGameLogicException exp) {
                game.sendErrorMessage(exp);
            }
        } else {
            setAsFailed();
            game.sendErrorMessage("Seed mission action attempted in a non-1E game");
        }
    }

}