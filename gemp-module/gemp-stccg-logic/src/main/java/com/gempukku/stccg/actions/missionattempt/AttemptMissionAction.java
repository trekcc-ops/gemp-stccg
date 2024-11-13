package com.gempukku.stccg.actions.missionattempt;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.choose.ChooseAwayTeamEffect;
import com.gempukku.stccg.actions.turn.AllowResponsesAction;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.AwayTeam;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.condition.missionrequirements.MissionRequirement;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.gamestate.ST1EGameState;

import java.util.*;
import java.util.stream.Stream;

public class AttemptMissionAction extends ActionyAction {
    private AttemptingUnit _attemptingUnit;
    private final MissionCard _missionCard;
    private final Collection<PhysicalCard> _revealedCards = new LinkedList<>();
    private final Collection<PhysicalCard> _encounteredCards = new LinkedList<>();

    private enum Progress {
        choseAttemptingUnit, startedMissionAttempt, solvedMission, failedMissionAttempt, endedMissionAttempt
    }

    public AttemptMissionAction(Player player, MissionCard missionCard) {
        super(player, "Attempt mission", ActionType.ATTEMPT_MISSION, Progress.values());
        _missionCard = missionCard;
    }

    @Override
    public PhysicalCard getCardForActionSelection() { return _missionCard; }
    @Override
    public PhysicalCard getActionSource() { return _missionCard; }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        Player player = cardGame.getPlayer(_performingPlayerId);
        return _missionCard.mayBeAttemptedByPlayer(player);
    }

    @Override
    public Action nextAction(DefaultGame cardGame) {
        Player player = cardGame.getPlayer(_performingPlayerId);

        Action cost = getNextCost();
        if (cost != null)
            return cost;

        if (!getProgress(Progress.choseAttemptingUnit)) {

            Map<String, AttemptingUnit> attemptingUnitMap = new HashMap<>();

            // Get Away Teams that can attempt mission
            Stream<AwayTeam> awayTeamOptions = _missionCard.getYourAwayTeamsOnSurface(player).filter(
                    awayTeam -> awayTeam.canAttemptMission(_missionCard));
            awayTeamOptions.forEach(awayTeam ->
                    attemptingUnitMap.put(awayTeam.concatenateAwayTeam(), awayTeam));

            // Get ships that can attempt mission
            for (PhysicalCard card : Filters.filterYourActive(player,
                    Filters.ship, Filters.atLocation(_missionCard.getLocation()))) {
                if (card instanceof PhysicalShipCard ship)
                    if (ship.canAttemptMission(_missionCard))
                        attemptingUnitMap.put(ship.getTitle(), ship);
            }

            Effect chooseAwayTeamEffect = new ChooseAwayTeamEffect(player,
                    attemptingUnitMap.keySet().stream().toList()) {
                @Override
                protected void awayTeamChosen(String result) {
                    setAttemptingUnit(attemptingUnitMap.get(result));
                }
            };
            return new SubAction(this, chooseAwayTeamEffect);
        }

        if (!getProgress(Progress.startedMissionAttempt)) {
            setProgress(Progress.startedMissionAttempt,true);
            return new AllowResponsesAction(
                    cardGame, this, EffectResult.Type.START_OF_MISSION_ATTEMPT);
        }

        if (_attemptingUnit.getAttemptingPersonnel().isEmpty()) {
            failMission(cardGame);
        }

        List<PhysicalCard> seedCards = _missionCard.getCardsSeededUnderneath();
        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);

        if (!getProgress(Progress.endedMissionAttempt)) {

            if (!seedCards.isEmpty()) {
                PhysicalCard firstSeedCard = seedCards.getFirst();
                if (!_revealedCards.contains(firstSeedCard)) {
                    _revealedCards.add(firstSeedCard);
                    return new RevealSeedCardAction(performingPlayer, firstSeedCard, _missionCard);
                }
                if (!_encounteredCards.contains(firstSeedCard)) {
                    _encounteredCards.add(firstSeedCard);
                    return new EncounterSeedCardAction(performingPlayer, firstSeedCard, _missionCard, _attemptingUnit);
                }
            }

            if (seedCards.isEmpty()) {
                if (cardGame.getModifiersQuerying().canPlayerSolveMission(_performingPlayerId, _missionCard)) {
                    MissionRequirement requirement = _missionCard.getRequirements();
                    if (requirement.canBeMetBy(_attemptingUnit.getAttemptingPersonnel())) {
                        solveMission(cardGame);
                    } else {
                        failMission(cardGame);
                    }
                }
            }
            setProgress(Progress.endedMissionAttempt, true);
        }

        return getNextAction();
    }

    private void solveMission(DefaultGame cardGame) {
        ST1EGameState gameState = (ST1EGameState) cardGame.getGameState();
        setProgress(Progress.solvedMission, true);
        _missionCard.setCompleted(true);
        cardGame.sendMessage(_performingPlayerId + " solved " + _missionCard.getCardLink());
        cardGame.getGameState().getPlayer(_performingPlayerId).scorePoints(_missionCard.getPoints());
        gameState.checkVictoryConditions();
    }

    public PhysicalCard getMission() { return _missionCard; }

    public void setAttemptingUnit(AttemptingUnit attemptingUnit) {
        _attemptingUnit = attemptingUnit;
        _progressIndicators.put(Progress.choseAttemptingUnit.name(), true);
    }

    public boolean isFailed() { return getProgress(Progress.failedMissionAttempt); }

    private void failMission(DefaultGame game) {
        setProgress(Progress.failedMissionAttempt, true);
        setProgress(Progress.endedMissionAttempt,true);
        game.sendMessage(_performingPlayerId + " failed mission attempt of " + _missionCard.getCardLink());
    }


}