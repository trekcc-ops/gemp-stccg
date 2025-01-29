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
import com.gempukku.stccg.game.*;
import com.gempukku.stccg.gamestate.GameLocation;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.List;

public class AttemptMissionAction extends ActionyAction implements TopLevelSelectableAction {
    private AttemptingUnitResolver _attemptingUnitTarget;
    private final MissionCard _missionCard;
    private PhysicalCard _lastCardRevealed;
    private PhysicalCard _lastCardEncountered;

    private enum Progress {
        choseAttemptingUnit, startedMissionAttempt, solvedMission, failedMissionAttempt, endedMissionAttempt
    }

    public AttemptMissionAction(DefaultGame cardGame, Player player, MissionLocation mission)
            throws InvalidGameLogicException {
        super(cardGame, player, "Attempt mission", ActionType.ATTEMPT_MISSION, Progress.values());
        _missionCard = mission.getMissionForPlayer(player.getPlayerId());
    }


    @Override
    public int getCardIdForActionSelection() { return _missionCard.getCardId(); }
    @Override
    public PhysicalCard getPerformingCard() { return _missionCard; }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        try {
            ST1EGame stGame;
            if (cardGame instanceof ST1EGame)
                stGame = (ST1EGame) cardGame;
            else throw new InvalidGameLogicException("Could not check mission attempt requirements for non-1E game");
            GameLocation missionLocation = _missionCard.getGameLocation();
            Player player = cardGame.getPlayer(_performingPlayerId);
            return missionLocation.mayBeAttemptedByPlayer(player, stGame);
        } catch(InvalidGameLogicException | PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            return false;
        }
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        MissionLocation missionLocation;
        if (_missionCard.getGameLocation() instanceof MissionLocation mission)
            missionLocation = mission;
        else throw new InvalidGameLogicException("Unable to identify a mission for card " + _missionCard.getTitle());

        Player player = cardGame.getPlayer(_performingPlayerId);

        Action cost = getNextCost();
        if (cost != null)
            return cost;

        if (!getProgress(Progress.choseAttemptingUnit)) {
            if (_attemptingUnitTarget == null) {

                List<AttemptingUnit> eligibleUnits = new ArrayList<>();
                missionLocation.getYourAwayTeamsOnSurface((ST1EGame) cardGame, player)
                        .filter(awayTeam -> awayTeam.canAttemptMission(cardGame, missionLocation))
                        .forEach(eligibleUnits::add);

                // Get ships that can attempt mission
                for (PhysicalCard card : Filters.filterYourActive(cardGame, player,
                        Filters.ship, Filters.atLocation(missionLocation))) {
                    if (card instanceof PhysicalShipCard ship)
                        if (ship.canAttemptMission(missionLocation))
                            eligibleUnits.add(ship);
                }
                if (eligibleUnits.size() > 1) {
                    _attemptingUnitTarget =
                            new AttemptingUnitResolver(new SelectAttemptingUnitAction(cardGame, player, eligibleUnits));
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
                return new AllowResponsesAction(cardGame, ActionResult.Type.START_OF_MISSION_ATTEMPT);
            }

            if (attemptingUnit.getAttemptingPersonnel().isEmpty()) {
                failMission(cardGame);
            }

            List<PhysicalCard> seedCards = missionLocation.getSeedCards();
            Player performingPlayer = cardGame.getPlayer(_performingPlayerId);

            if (!getProgress(Progress.endedMissionAttempt)) {

                if (!seedCards.isEmpty()) {
                    PhysicalCard firstSeedCard = seedCards.getFirst();
                    if (_lastCardRevealed != firstSeedCard) {
                        _lastCardRevealed = firstSeedCard;
                        return new RevealSeedCardAction(
                                performingPlayer, firstSeedCard, this, missionLocation);
                    } else if (_lastCardEncountered != firstSeedCard) {
                        _lastCardEncountered = firstSeedCard;
                        return new EncounterSeedCardAction(cardGame,
                                performingPlayer, firstSeedCard, attemptingUnit, this, missionLocation);
                    } else {
                        throw new InvalidGameLogicException(firstSeedCard.getTitle() +
                                " was already encountered, but not removed from under the mission");
                    }
                } else  {
                    if (cardGame.getModifiersQuerying().canPlayerSolveMission(_performingPlayerId, missionLocation)) {
                        MissionRequirement requirement = missionLocation.getRequirements(_performingPlayerId);
                        if (requirement.canBeMetBy(attemptingUnit.getAttemptingPersonnel())) {
                            solveMission(missionLocation, cardGame);
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
        cardGame.sendMessage(_performingPlayerId + " solved " + _missionCard.getCardLink());
    }

    public GameLocation getLocation() throws InvalidGameLogicException { return _missionCard.getGameLocation(); }

    public void setAttemptingUnit(AttemptingUnit attemptingUnit) {
        _attemptingUnitTarget = new AttemptingUnitResolver(attemptingUnit);
        setProgress(Progress.choseAttemptingUnit);
    }

    private void failMission(DefaultGame game) {
        setProgress(Progress.failedMissionAttempt);
        setProgress(Progress.endedMissionAttempt);
        setAsFailed();
        game.sendMessage(_performingPlayerId + " failed mission attempt of " + _missionCard.getCardLink());
    }


}