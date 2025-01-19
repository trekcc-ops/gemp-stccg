package com.gempukku.stccg.actions.missionattempt;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.choose.SelectAttemptingUnitAction;
import com.gempukku.stccg.actions.turn.AllowResponsesAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.condition.missionrequirements.MissionRequirement;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class AttemptMissionAction extends ActionyAction implements TopLevelSelectableAction {
    private AttemptingUnitResolver _attemptingUnitTarget;
    private final MissionCard _missionCard;
    private PhysicalCard _lastCardRevealed;
    private PhysicalCard _lastCardEncountered;

    private enum Progress {
        choseAttemptingUnit, startedMissionAttempt, solvedMission, failedMissionAttempt, endedMissionAttempt
    }

    public AttemptMissionAction(Player player, MissionLocation mission) throws InvalidGameLogicException {
        super(player, "Attempt mission", ActionType.ATTEMPT_MISSION, Progress.values());
        _missionCard = mission.getMissionForPlayer(player.getPlayerId());
    }


    @Override
    public int getCardIdForActionSelection() { return _missionCard.getCardId(); }
    @Override
    public PhysicalCard getPerformingCard() { return _missionCard; }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        try {
            MissionLocation missionLocation = _missionCard.getLocation();
            Player player = cardGame.getPlayer(_performingPlayerId);
            return missionLocation.mayBeAttemptedByPlayer(player);
        } catch(InvalidGameLogicException exp) {
            cardGame.sendErrorMessage(exp);
            return false;
        }
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
        MissionLocation missionLocation = _missionCard.getLocation();
        Player player = cardGame.getPlayer(_performingPlayerId);

        Action cost = getNextCost();
        if (cost != null)
            return cost;

        if (!getProgress(Progress.choseAttemptingUnit)) {
            if (_attemptingUnitTarget == null) {

                List<AttemptingUnit> eligibleUnits = new ArrayList<>();
                missionLocation.getYourAwayTeamsOnSurface(player)
                        .filter(awayTeam -> awayTeam.canAttemptMission(missionLocation))
                        .forEach(eligibleUnits::add);

                // Get ships that can attempt mission
                for (PhysicalCard card : Filters.filterYourActive(player,
                        Filters.ship, Filters.atLocation(missionLocation))) {
                    if (card instanceof PhysicalShipCard ship)
                        if (ship.canAttemptMission(missionLocation))
                            eligibleUnits.add(ship);
                }
                if (eligibleUnits.size() > 1) {
                    _attemptingUnitTarget =
                            new AttemptingUnitResolver(new SelectAttemptingUnitAction(player, eligibleUnits));
                    return _attemptingUnitTarget.getSelectionAction();
                } else {
                    _attemptingUnitTarget = new AttemptingUnitResolver(Iterables.getOnlyElement(eligibleUnits));
                }
            } else {
                _attemptingUnitTarget.resolve();
            }
        }

        AttemptingUnit attemptingUnit = _attemptingUnitTarget.getAttemptingUnit();


        if (attemptingUnit.getAttemptingPersonnel().isEmpty()) {
            failMission(cardGame);
        } else {

            if (!getProgress(Progress.startedMissionAttempt)) {
                setProgress(Progress.startedMissionAttempt);
                return new AllowResponsesAction(
                        cardGame, ActionResult.Type.START_OF_MISSION_ATTEMPT);
            }

            if (attemptingUnit.getAttemptingPersonnel().isEmpty()) {
                failMission(cardGame);
            }

            List<PhysicalCard> seedCards = missionLocation.getCardsSeededUnderneath();
            Player performingPlayer = cardGame.getPlayer(_performingPlayerId);

            if (!getProgress(Progress.endedMissionAttempt)) {

                if (!seedCards.isEmpty()) {
                    PhysicalCard firstSeedCard = seedCards.getFirst();
                    if (_lastCardRevealed != firstSeedCard) {
                        _lastCardRevealed = firstSeedCard;
                        return new RevealSeedCardAction(performingPlayer, firstSeedCard, this);
                    } else if (_lastCardEncountered != firstSeedCard) {
                        _lastCardEncountered = firstSeedCard;
                        return new EncounterSeedCardAction(performingPlayer, firstSeedCard, this);
                    } else {
                        throw new InvalidGameLogicException(firstSeedCard.getTitle() + " has already been encountered, but has not been removed");
                    }
                } else  {
                    if (cardGame.getModifiersQuerying().canPlayerSolveMission(_performingPlayerId, missionLocation)) {
                        MissionRequirement requirement = missionLocation.getRequirements(_performingPlayerId);
                        if (requirement.canBeMetBy(attemptingUnit.getAttemptingPersonnel())) {
                            solveMission(cardGame);
                        } else {
                            failMission(cardGame);
                        }
                    }
                }
                setProgress(Progress.endedMissionAttempt);
            }
        }
        return getNextAction();
    }

    private void solveMission(DefaultGame cardGame) throws InvalidGameLogicException {
        setProgress(Progress.solvedMission);
        MissionLocation missionLocation = _missionCard.getLocation();
        missionLocation.complete(_performingPlayerId);
        cardGame.sendMessage(_performingPlayerId + " solved " + _missionCard.getCardLink());
    }

    public MissionLocation getMission() throws InvalidGameLogicException { return _missionCard.getLocation(); }

    public void setAttemptingUnit(AttemptingUnit attemptingUnit) {
        _attemptingUnitTarget = new AttemptingUnitResolver(attemptingUnit);
        setProgress(Progress.choseAttemptingUnit);
    }

    public boolean isFailed() { return getProgress(Progress.failedMissionAttempt); }

    private void failMission(DefaultGame game) {
        setProgress(Progress.failedMissionAttempt);
        setProgress(Progress.endedMissionAttempt);
        game.sendMessage(_performingPlayerId + " failed mission attempt of " + _missionCard.getCardLink());
    }

    public AttemptingUnit getAttemptingUnit() throws InvalidGameLogicException {
        if (_attemptingUnitTarget == null || !_attemptingUnitTarget.isResolved()) {
            throw new InvalidGameLogicException("Attempting unit for mission attempt not yet resolved");
        } else {
            return _attemptingUnitTarget.getAttemptingUnit();
        }
    }


}