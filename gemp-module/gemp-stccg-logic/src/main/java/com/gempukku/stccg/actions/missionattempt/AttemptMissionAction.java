package com.gempukku.stccg.actions.missionattempt;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.choose.SelectAttemptingUnitAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
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
import java.util.List;

public class AttemptMissionAction extends ActionyAction implements TopLevelSelectableAction {
    private AttemptingUnitResolver _attemptingUnitTarget;

    @JsonProperty("targetCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final MissionCard _performingCard;
    private PhysicalCard _lastCardRevealed;
    private PhysicalCard _lastCardEncountered;
    private final int _locationId;

    private enum Progress {
        choseAttemptingUnit, startedMissionAttempt, solvedMission, failedMissionAttempt, endedMissionAttempt
    }

    public AttemptMissionAction(DefaultGame cardGame, Player player, MissionCard cardForAction, MissionLocation mission)
            throws InvalidGameLogicException {
        super(cardGame, player.getPlayerId(), ActionType.ATTEMPT_MISSION, Progress.values());
        _performingCard = cardForAction;
        _locationId = mission.getLocationId();
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
    protected void continueInitiation(DefaultGame cardGame) {
        try {
            Action nextAction = nextActionOld(cardGame);
            if (nextAction == null) {
                setAsInitiated();
            } else {
                cardGame.addActionToStack(nextAction);
            }
        } catch(InvalidGameLogicException | PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            setAsFailed();
        }
    }

    public Action nextActionOld(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        if (cardGame instanceof ST1EGame stGame) {
            MissionLocation missionLocation;
            GameLocation gameLocation = stGame.getGameState().getLocationById(_locationId);
            if (gameLocation instanceof MissionLocation mission) {
                missionLocation = mission;
            } else {
                throw new InvalidGameLogicException("Unable to locate mission with location id " + _locationId);
            }

            Player player = cardGame.getPlayer(_performingPlayerId);
            if (!getProgress(Progress.choseAttemptingUnit)) {
                if (_attemptingUnitTarget == null) {

                    List<AttemptingUnit> eligibleUnits = new ArrayList<>();
                    missionLocation.getYourAwayTeamsOnSurface(stGame, player)
                            .filter(awayTeam -> awayTeam.canAttemptMission(cardGame, missionLocation))
                            .forEach(eligibleUnits::add);

                    // Get ships that can attempt mission
                    for (PhysicalCard card : Filters.filterYourCardsInPlay(cardGame, player,
                            Filters.ship, Filters.atLocation(missionLocation))) {
                        if (card instanceof ShipCard ship) {
                            boolean canShipAttempt = stGame.getRules().canShipAttemptMission(ship,
                                    _locationId, stGame, _performingPlayerId);
                            if (canShipAttempt) {
                                eligibleUnits.add(ship);
                            }
                        }
                    }
                    if (eligibleUnits.size() > 1) {
                        String selectionText = (missionLocation.isPlanet()) ? "Choose an Away Team" : "Choose a ship";
                        _attemptingUnitTarget = new AttemptingUnitResolver(
                                new SelectAttemptingUnitAction(cardGame, player, eligibleUnits, selectionText));
                        return _attemptingUnitTarget.getSelectionAction();
                    } else {
                        _attemptingUnitTarget = new AttemptingUnitResolver(Iterables.getOnlyElement(eligibleUnits));
                        setProgress(Progress.choseAttemptingUnit);
                    }
                } else {
                    _attemptingUnitTarget.resolve();
                    setProgress(Progress.choseAttemptingUnit);
                }
            }

            if (getProgress(Progress.choseAttemptingUnit)) {
                AttemptingUnit attemptingUnit = _attemptingUnitTarget.getAttemptingUnit();
                if (attemptingUnit.getAttemptingPersonnel(cardGame).isEmpty()) {
                    failMission(cardGame);
                } else  {
                    saveResult(new ActionResult(ActionResult.Type.START_OF_MISSION_ATTEMPT, this), cardGame);
                }
                return null;
            } else {
                throw new InvalidGameLogicException("Unable to resolve personnel to attempt mission");
            }
        } else {
            setAsFailed();
            return null;
        }
    }

    protected void processEffect(DefaultGame cardGame) {
        try {
            if (cardGame instanceof ST1EGame stGame) {
                AttemptingUnit attemptingUnit = _attemptingUnitTarget.getAttemptingUnit();
                MissionLocation missionLocation;
                GameLocation gameLocation = stGame.getGameState().getLocationById(_locationId);
                if (gameLocation instanceof MissionLocation mission) {
                    missionLocation = mission;
                } else {
                    throw new InvalidGameLogicException("Unable to locate mission with location id " + _locationId);
                }
                if (attemptingUnit.getAttemptingPersonnel(cardGame).isEmpty()) {
                    failMission(cardGame);
                }

                List<PhysicalCard> seedCards = missionLocation.getSeedCards();

                if (!seedCards.isEmpty()) {
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
                } else {
                    if (cardGame.canPlayerSolveMission(_performingPlayerId, missionLocation)) {
                        MissionRequirement requirement = missionLocation.getRequirements(_performingPlayerId);
                        if (requirement.canBeMetBy(attemptingUnit.getAttemptingPersonnel(cardGame), cardGame)) {
                            solveMission(missionLocation, cardGame);
                        } else {
                            failMission(cardGame);
                        }
                    } else {
                        failMission(cardGame);
                    }
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

    private void solveMission(MissionLocation mission, DefaultGame cardGame)
            throws InvalidGameLogicException, PlayerNotFoundException {
        setProgress(Progress.solvedMission);
        setAsSuccessful();
        mission.complete(_performingPlayerId, cardGame);
    }

    public void setAttemptingUnit(AttemptingUnit attemptingUnit) {
        _attemptingUnitTarget = new AttemptingUnitResolver(attemptingUnit);
        setProgress(Progress.choseAttemptingUnit);
    }

    private void failMission(DefaultGame game) {
        setProgress(Progress.failedMissionAttempt);
        setProgress(Progress.endedMissionAttempt);
        setAsFailed();
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

}