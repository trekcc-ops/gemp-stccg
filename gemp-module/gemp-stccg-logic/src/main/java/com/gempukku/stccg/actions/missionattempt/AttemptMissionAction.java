package com.gempukku.stccg.actions.missionattempt;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.ActionResult;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class AttemptMissionAction extends ActionyAction {
    private AttemptingUnit _attemptingUnit;
    private SelectAttemptingUnitAction _selectAttemptingUnitAction;
    private final MissionCard _missionCard;
    private final MissionLocation _missionLocation;
    private final Collection<PhysicalCard> _revealedCards = new LinkedList<>();
    private final Collection<PhysicalCard> _encounteredCards = new LinkedList<>();

    private enum Progress {
        choseAttemptingUnit, startedMissionAttempt, solvedMission, failedMissionAttempt, endedMissionAttempt
    }

    public AttemptMissionAction(Player player, MissionLocation mission) throws InvalidGameLogicException {
        super(player, "Attempt mission", ActionType.ATTEMPT_MISSION, Progress.values());
        _missionLocation = mission;
        _missionCard = mission.getMissionForPlayer(player.getPlayerId());
    }


    @Override
    public PhysicalCard getCardForActionSelection() { return _missionCard; }
    @Override
    public PhysicalCard getPerformingCard() { return _missionCard; }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        try {
            Player player = cardGame.getPlayer(_performingPlayerId);
            return _missionLocation.mayBeAttemptedByPlayer(player);
        } catch(InvalidGameLogicException exp) {
            cardGame.sendErrorMessage(exp);
            return false;
        }
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {

            Player player = cardGame.getPlayer(_performingPlayerId);

            Action cost = getNextCost();
            if (cost != null)
                return cost;

            if (!getProgress(Progress.choseAttemptingUnit)) {
                if (_selectAttemptingUnitAction == null) {

                    List<AttemptingUnit> eligibleUnits = new ArrayList<>();
                    _missionLocation.getYourAwayTeamsOnSurface(player)
                            .filter(awayTeam -> awayTeam.canAttemptMission(_missionLocation))
                            .forEach(eligibleUnits::add);

                    // Get ships that can attempt mission
                    for (PhysicalCard card : Filters.filterYourActive(player,
                            Filters.ship, Filters.atLocation(_missionLocation))) {
                        if (card instanceof PhysicalShipCard ship)
                            if (ship.canAttemptMission(_missionLocation))
                                eligibleUnits.add(ship);
                    }
                    _selectAttemptingUnitAction = new SelectAttemptingUnitAction(player, eligibleUnits);
                    return _selectAttemptingUnitAction;
                } else if (_selectAttemptingUnitAction.wasCarriedOut()) {
                    setAttemptingUnit(_selectAttemptingUnitAction.getSelection());
                }
            }

            if (_attemptingUnit.getAttemptingPersonnel().isEmpty()) {
                failMission(cardGame);
            }

            if (!getProgress(Progress.startedMissionAttempt)) {
                setProgress(Progress.startedMissionAttempt);
                return new AllowResponsesAction(
                        cardGame, ActionResult.Type.START_OF_MISSION_ATTEMPT);
            }

            if (_attemptingUnit.getAttemptingPersonnel().isEmpty()) {
                failMission(cardGame);
            }

            List<PhysicalCard> seedCards = _missionLocation.getCardsSeededUnderneath();
            Player performingPlayer = cardGame.getPlayer(_performingPlayerId);

            if (!getProgress(Progress.endedMissionAttempt)) {

                if (!seedCards.isEmpty()) {
                    PhysicalCard firstSeedCard = seedCards.getFirst();
                    if (!_revealedCards.contains(firstSeedCard)) {
                        _revealedCards.add(firstSeedCard);
                        return new RevealSeedCardAction(performingPlayer, firstSeedCard, _missionLocation);
                    }
                    if (!_encounteredCards.contains(firstSeedCard)) {
                        _encounteredCards.add(firstSeedCard);
                        return new EncounterSeedCardAction(this, performingPlayer, firstSeedCard,
                                _missionLocation, _attemptingUnit);
                    }
                }

                if (seedCards.isEmpty()) {
                    if (cardGame.getModifiersQuerying().canPlayerSolveMission(_performingPlayerId, _missionLocation)) {
                        MissionRequirement requirement = _missionLocation.getRequirements(_performingPlayerId);
                        if (requirement.canBeMetBy(_attemptingUnit.getAttemptingPersonnel())) {
                            solveMission(cardGame);
                        } else {
                            failMission(cardGame);
                        }
                    }
                }
                setProgress(Progress.endedMissionAttempt);
            }
        return getNextAction();
    }

    private void solveMission(DefaultGame cardGame) throws InvalidGameLogicException {
        setProgress(Progress.solvedMission);
        _missionLocation.complete(_performingPlayerId);
        cardGame.sendMessage(_performingPlayerId + " solved " + _missionCard.getCardLink());
    }

    public MissionLocation getMission() { return _missionLocation; }

    public void setAttemptingUnit(AttemptingUnit attemptingUnit) {
        _attemptingUnit = attemptingUnit;
        _progressIndicators.put(Progress.choseAttemptingUnit.name(), true);
    }

    public boolean isFailed() { return getProgress(Progress.failedMissionAttempt); }

    private void failMission(DefaultGame game) {
        setProgress(Progress.failedMissionAttempt);
        setProgress(Progress.endedMissionAttempt);
        game.sendMessage(_performingPlayerId + " failed mission attempt of " + _missionCard.getCardLink());
    }


}