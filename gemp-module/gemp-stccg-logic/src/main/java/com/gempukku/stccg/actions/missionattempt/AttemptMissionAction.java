package com.gempukku.stccg.actions.missionattempt;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.choose.SelectAttemptingUnitAction;
import com.gempukku.stccg.actions.scorepoints.ScorePointsAction;
import com.gempukku.stccg.actions.targetresolver.AttemptingUnitResolver;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.condition.missionrequirements.MissionRequirement;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.GameLocation;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AttemptMissionAction extends ActionWithSubActions implements TopLevelSelectableAction,
        ActionWithRespondableInitiation {
    private AttemptingUnitResolver _attemptingUnitTarget;
    @JsonProperty("targetCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final MissionCard _performingCard;
    private PhysicalCard _lastCardRevealed;
    private PhysicalCard _lastCardEncountered;
    private final int _locationId;
    private boolean _successDetermined;
    private boolean _missionSpecHelped;
    private boolean _failedToOvercomeCondition;

    public AttemptMissionAction(DefaultGame cardGame, Player player, MissionCard cardForAction,
                                MissionLocation mission) throws InvalidGameLogicException {
        super(cardGame, player.getPlayerId(), ActionType.ATTEMPT_MISSION,
                new GameTextContext(cardForAction, player.getPlayerId()));
        _performingCard = cardForAction;
        _locationId = mission.getLocationId();

        if (cardGame instanceof ST1EGame stGame) {
            List<AttemptingUnit> eligibleUnits = new ArrayList<>();
            mission.getYourAwayTeamsOnSurface(stGame, player)
                    .filter(awayTeam -> awayTeam.canAttemptMission(cardGame, mission))
                    .forEach(eligibleUnits::add);

            // Get ships that can attempt mission
            for (PhysicalCard card : Filters.filterYourCardsInPlay(cardGame, player,
                    Filters.ship, Filters.atLocation(mission))) {
                if (card instanceof ShipCard ship) {
                    boolean canShipAttempt = stGame.getRules().canShipAttemptMission(ship,
                            _locationId, stGame, _performingPlayerId);
                    if (canShipAttempt) {
                        eligibleUnits.add(ship);
                    }
                }
            }
            String selectionText = (mission.isPlanet()) ? "Choose an Away Team" : "Choose a ship";
            _attemptingUnitTarget = new AttemptingUnitResolver(
                    new SelectAttemptingUnitAction(cardGame, player, eligibleUnits, selectionText));
            _cardTargets.add(_attemptingUnitTarget);
        } else {
            setAsFailed();
        }
    }


    @Override
    public PhysicalCard getPerformingCard() { return _performingCard; }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        try {
            if (cardGame instanceof ST1EGame stGame) {
                GameLocation location = stGame.getGameState().getLocationById(_locationId);
                if (location instanceof MissionLocation missionLocation) {
                    Player player = cardGame.getPlayer(_performingPlayerId);
                    return missionLocation.mayBeAttemptedByPlayer(player, stGame);
                }
            }
            throw new InvalidGameLogicException("Could not check mission attempt requirements for non-mission location");
        } catch(InvalidGameLogicException | PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            return false;
        }
    }

    @Override
    public void saveInitiationResult(DefaultGame cardGame) {
        saveResult(new MissionAttemptStartedResult(cardGame, _performingPlayerId, this, getMission()), cardGame);
    }

    protected void processEffect(DefaultGame cardGame) {
        try {
            if (_failedToOvercomeCondition) {
                if (_currentSubAction != null) {
                    _processedSubActions.add(_currentSubAction);
                    _currentSubAction = null;
                } else if (!_queuedSubActions.isEmpty()) {
                    _currentSubAction = _queuedSubActions.getFirst().createAction(cardGame, _actionContext);
                    _queuedSubActions.removeFirst();
                    cardGame.addActionToStack(_currentSubAction);
                } else {
                    setAsFailed(cardGame);
                }
            } else if (cardGame instanceof ST1EGame stGame) {
                AttemptingUnit attemptingUnit = _attemptingUnitTarget.getAttemptingUnit();
                MissionLocation missionLocation;
                GameLocation gameLocation = stGame.getGameState().getLocationById(_locationId);
                if (gameLocation instanceof MissionLocation mission) {
                    missionLocation = mission;
                } else {
                    throw new InvalidGameLogicException("Unable to locate mission with location id " + _locationId);
                }
                if (attemptingUnit.getAttemptingPersonnel(cardGame).isEmpty()) {
                    setAsFailed(cardGame);
                }

                List<PhysicalCard> seedCards = missionLocation.getSeedCards();

                if (!seedCards.isEmpty() && !wasFailed()) {
                    PhysicalCard firstSeedCard = seedCards.getFirst();
                    if (_lastCardRevealed != firstSeedCard) {
                        _lastCardRevealed = firstSeedCard;
                        cardGame.addActionToStack(new RevealSeedCardAction(cardGame, _performingPlayerId, firstSeedCard,
                                missionLocation));
                    } else if (_lastCardEncountered != firstSeedCard) {
                        _lastCardEncountered = firstSeedCard;
                        List<Action> encounterActions = firstSeedCard.getEncounterActions(
                                cardGame, this, attemptingUnit, missionLocation);
                        if (encounterActions.size() != 1) {
                            throw new InvalidGameLogicException("Unable to identify seed card actions");
                        } else {
                            cardGame.addActionToStack(Iterables.getOnlyElement(encounterActions));
                        }
                    } else {
                        throw new InvalidGameLogicException(firstSeedCard.getTitle() +
                                " was already encountered, but not removed from under the mission");
                    }
                } else if (!wasFailed() && !_successDetermined) {
                    _successDetermined = true;
                    if (cardGame.canPlayerSolveMission(_performingPlayerId, missionLocation)) {
                        MissionRequirement requirement = missionLocation.getRequirements(_performingPlayerId);
                        Collection<PersonnelCard> personnelAttempting = attemptingUnit.getAttemptingPersonnel(cardGame);

                        if (requirement.canBeMetBy(personnelAttempting, cardGame, _actionContext)) {
                            _missionSpecHelped = requirement
                                    .canBeMetWithMissionSpecialistHelping(personnelAttempting, cardGame, _actionContext);
                            mission.setAsCompleted();
                            Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
                            performingPlayer.recordSolvedMission(getMission());
                            cardGame.addActionToStack(
                                    new ScorePointsAction(cardGame, getMission(), _performingPlayerId, getMission().getPoints(), _actionContext));
                        } else {
                            setAsFailed(cardGame);
                        }
                    } else {
                        setAsFailed(cardGame);
                    }
                } else if (!wasFailed()) {
                    saveResult(new MissionAttemptEndedResult(cardGame, true, this, _performingCard, _missionSpecHelped), cardGame);
                    setAsSuccessful();
                }
            } else {
                cardGame.sendErrorMessage("Cannot attempt a mission in a non-1E game");
                setAsFailed();
            }
        } catch(InvalidGameLogicException | PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            setAsFailed();
        }
    }

    public void setAttemptingUnit(AttemptingUnit attemptingUnit) {
        _attemptingUnitTarget = new AttemptingUnitResolver(attemptingUnit);
    }

    public AttemptingUnit getAttemptingUnit() throws InvalidGameLogicException {
        return _attemptingUnitTarget.getAttemptingUnit();
    }

    public int getLocationId() { return _locationId; }

    public boolean isFirstAttemptForPlayerOfThisMission(DefaultGame cardGame) {
        for (Action action : cardGame.getActionsEnvironment().getPerformedActions()) {
            if (action instanceof AttemptMissionAction missionAction &&
                    action.getActionId() < getActionId() &&
                    missionAction.getLocationId() == _locationId &&
                    action.getPerformingPlayerId().equals(_performingPlayerId) &&
                    action.wasInitiated()) {
                return false;
            }
        }
        return true;
    }

    @JsonIgnore
    public MissionCard getMission() {
        return _performingCard;
    }

    public void setAsConditionFailed() {
        _failedToOvercomeCondition = true;
    }

    public void setAsFailed(DefaultGame cardGame) {
        saveResult(new MissionAttemptEndedResult(cardGame, false, this, getMission(), false), cardGame);
        super.setAsFailed();
    }
}