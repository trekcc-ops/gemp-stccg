package com.gempukku.stccg.actions.playcard;

import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.targetresolver.ReportCardResolver;
import com.gempukku.stccg.actions.usage.UseNormalCardPlayAction;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ReportCardAction extends PlayCardAction {
    private final ReportCardResolver _targetResolver;

    public ReportCardAction(DefaultGame cardGame, ReportableCard cardToPlay, boolean forFree) {
        super(cardGame, cardToPlay, cardToPlay, cardToPlay.getOwnerName(), null, ActionType.PLAY_CARD);
        if (!forFree)
            appendCost(new UseNormalCardPlayAction(cardGame, _performingPlayerId));
        _targetResolver = new ReportCardResolver(cardGame, cardToPlay);
        _cardTargets.add(_targetResolver);
    }

    public ReportCardAction(DefaultGame cardGame, ReportableCard cardToPlay, boolean forFree,
                            Map<PhysicalCard, List<Affiliation>> calculatedDestinationMap) {
        super(cardGame, cardToPlay, cardToPlay, cardToPlay.getOwnerName(), null, ActionType.PLAY_CARD);
        if (!forFree)
            appendCost(new UseNormalCardPlayAction(cardGame, _performingPlayerId));
        _targetResolver = new ReportCardResolver(cardToPlay, calculatedDestinationMap);
        _cardTargets.add(_targetResolver);
    }



    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return super.requirementsAreMet(cardGame) && !_targetResolver.cannotBeResolved(cardGame);
    }

    public void processEffect(DefaultGame cardGame) {
        try {
            if (_cardEnteringPlay instanceof ReportableCard reportable) {
                Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
                if (reportable instanceof AffiliatedCard affiliatedCard) {
                    Affiliation chosenAffiliation = _targetResolver.getAffiliationToReportAs();
                    affiliatedCard.setCurrentAffiliation(chosenAffiliation);
                    performingPlayer.addPlayedAffiliation(chosenAffiliation);
                }
                setAsSuccessful();

                PhysicalCard destination = _targetResolver.getDestination();
                GameState gameState = cardGame.getGameState();

                cardGame.removeCardsFromZone(Collections.singleton(reportable));
                reportable.setLocationId(cardGame, destination.getLocationId());

                if (destination instanceof CardWithCrew cardWithCrew) {
                    // if reporting to a ship or facility
                    reportable.attachTo(destination);
                    gameState.addCardToZone(cardGame, reportable, Zone.ATTACHED, _actionContext);
                    if (reportable instanceof ShipCard ship && destination instanceof FacilityCard facility) {
                        ship.dockAtFacility(facility);
                    }
                } else if (reportable.getCardType() == CardType.SHIP) {
                    // if reporting a ship in space at a location
                    gameState.addCardToZone(cardGame, reportable, Zone.AT_LOCATION, _actionContext);
                } else {
                    // if reporting another reportable to a location
                    gameState.addCardToZone(cardGame, reportable, Zone.ATTACHED, _actionContext);
                    reportable.attachTo(destination);
                    reportable.setLocationId(cardGame, destination.getLocationId());
                    if (destination instanceof MissionCard missionDestination &&
                            cardGame instanceof ST1EGame stGame &&
                            missionDestination.getGameLocation(stGame) instanceof MissionLocation missionLocation) {
                        stGame.getGameState().addCardToEligibleAwayTeam(stGame, reportable, missionLocation);
                    }
                }

                saveResult(new PlayCardResult(this, _cardEnteringPlay), cardGame);
            } else {
                throw new InvalidGameLogicException("Tried to report a non-reportable card");
            }
        } catch(InvalidGameLogicException | PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            setAsFailed();
        }
    }

    public void setDestination(PhysicalCard card) {
        _targetResolver.setDestination(card);
    }

    public void setAffiliation(Affiliation affiliation) {
        _targetResolver.setAffiliation(affiliation);
    }


}