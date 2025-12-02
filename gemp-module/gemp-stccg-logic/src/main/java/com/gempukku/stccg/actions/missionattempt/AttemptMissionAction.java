package com.gempukku.stccg.actions.missionattempt;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.choose.SelectAttemptingUnitAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.condition.missionrequirements.MissionRequirement;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.*;
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
    private MissionLocation _missionLocation;


    private enum Progress {
        choseAttemptingUnit, startedMissionAttempt, solvedMission, failedMissionAttempt, endedMissionAttempt
    }

    public AttemptMissionAction(DefaultGame cardGame, Player player, MissionCard cardForAction, MissionLocation mission)
            throws InvalidGameLogicException {
        super(cardGame, player, "Attempt mission", ActionType.ATTEMPT_MISSION, Progress.values());
        _performingCard = cardForAction;
        _missionLocation = mission;
    }


    @Override
    public PhysicalCard getPerformingCard() { return _performingCard; }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        try {
            if (cardGame instanceof ST1EGame stGame) {
                GameLocation missionLocation = _missionLocation;
                Player player = cardGame.getPlayer(_performingPlayerId);
                return missionLocation.mayBeAttemptedByPlayer(player, stGame);
            }
            else throw new InvalidGameLogicException("Could not check mission attempt requirements for non-1E game");
        } catch(InvalidGameLogicException | PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            return false;
        }
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        Player player = cardGame.getPlayer(_performingPlayerId);

        Action cost = getNextCost();
        if (cost != null)
            return cost;

        if (!getProgress(Progress.choseAttemptingUnit)) {
            if (_attemptingUnitTarget == null) {

                List<AttemptingUnit> eligibleUnits = new ArrayList<>();
                _missionLocation.getYourAwayTeamsOnSurface((ST1EGame) cardGame, player)
                        .filter(awayTeam -> awayTeam.canAttemptMission(cardGame, _missionLocation))
                        .forEach(eligibleUnits::add);

                // Get ships that can attempt mission
                for (PhysicalCard card : Filters.filterYourCardsInPlay(cardGame, player,
                        Filters.ship, Filters.atLocation(_missionLocation))) {
                    if (card instanceof PhysicalShipCard ship)
                        if (ship.canAttemptMission(_missionLocation))
                            eligibleUnits.add(ship);
                }
                if (eligibleUnits.size() > 1) {
                    String selectionText = (_missionLocation.isPlanet()) ? "Choose an Away Team" : "Choose a ship";
                    _attemptingUnitTarget = new AttemptingUnitResolver(
                            new SelectAttemptingUnitAction(cardGame, player, eligibleUnits, selectionText));
                    return _attemptingUnitTarget.getSelectionAction();
                } else {
                    _attemptingUnitTarget = new AttemptingUnitResolver(Iterables.getOnlyElement(eligibleUnits));
                }
            } else {
                _attemptingUnitTarget.resolve();
            }
        }

        if (isBeingInitiated()) {
            setAsInitiated();
        }

        AttemptingUnit attemptingUnit = _attemptingUnitTarget.getAttemptingUnit();


        if (attemptingUnit.getAttemptingPersonnel().isEmpty()) {
            failMission(cardGame);
        }

        if (!wasFailed()) {

            if (!getProgress(Progress.startedMissionAttempt)) {
                setProgress(Progress.startedMissionAttempt);
                saveResult(new ActionResult(ActionResult.Type.START_OF_MISSION_ATTEMPT, this));
                return null;
            }

            if (attemptingUnit.getAttemptingPersonnel().isEmpty()) {
                failMission(cardGame);
            }

            List<PhysicalCard> seedCards = _missionLocation.getSeedCards();
            Player performingPlayer = cardGame.getPlayer(_performingPlayerId);

            if (!getProgress(Progress.endedMissionAttempt)) {

                if (!seedCards.isEmpty()) {
                    PhysicalCard firstSeedCard = seedCards.getFirst();
                    if (_lastCardRevealed != firstSeedCard) {
                        _lastCardRevealed = firstSeedCard;
                        return new RevealSeedCardAction(
                                performingPlayer, firstSeedCard, this, _missionLocation);
                    } else if (_lastCardEncountered != firstSeedCard) {
                        _lastCardEncountered = firstSeedCard;
                        List<Action> encounterActions = firstSeedCard.getEncounterActions(
                                cardGame, this, attemptingUnit, _missionLocation);
                        if (encounterActions.size() != 1) {
                            throw new InvalidGameLogicException("Unable to identify seed card actions");
                        } else {
                            return Iterables.getOnlyElement(encounterActions);
                        }
                    } else {
                        throw new InvalidGameLogicException(firstSeedCard.getTitle() +
                                " was already encountered, but not removed from under the mission");
                    }
                } else  {
                    if (cardGame.getGameState().getModifiersQuerying().canPlayerSolveMission(_performingPlayerId, _missionLocation)) {
                        MissionRequirement requirement = _missionLocation.getRequirements(_performingPlayerId);
                        if (requirement.canBeMetBy(attemptingUnit.getAttemptingPersonnel())) {
                            solveMission(_missionLocation, cardGame);
                        } else {
                            failMission(cardGame);
                        }
                    } else {
                        failMission(cardGame);
                    }
                }
                setProgress(Progress.endedMissionAttempt);
            }
        }
        return getNextAction();
    }

    private void solveMission(MissionLocation mission, DefaultGame cardGame)
            throws InvalidGameLogicException, PlayerNotFoundException {
        setProgress(Progress.solvedMission);
        setAsSuccessful();
        mission.complete(_performingPlayerId, cardGame);
    }

    public MissionLocation getLocation() { return _missionLocation; }

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



}