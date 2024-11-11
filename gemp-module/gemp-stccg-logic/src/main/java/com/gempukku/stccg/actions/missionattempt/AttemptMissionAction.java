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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class AttemptMissionAction extends ActionyAction {
    private AttemptingUnit _attemptingUnit;
    private final MissionCard _missionCard;
    private boolean _attemptingEntityWasChosen, _missionAttemptInitiated, _missionAttemptEnded;
    final Map<String, AttemptingUnit> _attemptingEntityMap = new HashMap<>();
    final List<PhysicalCard> _revealedCards = new LinkedList<>();
    final List<PhysicalCard> _encounteredCards = new LinkedList<>();

    public AttemptMissionAction(Player player, MissionCard missionCard) {
        super(player, "Attempt mission", ActionType.ATTEMPT_MISSION);
        _missionCard = missionCard;

        // Get Away Teams that can attempt mission
        Stream<AwayTeam> awayTeamOptions = missionCard.getYourAwayTeamsOnSurface(player).filter(
                awayTeam -> awayTeam.canAttemptMission(missionCard));
        awayTeamOptions.forEach(awayTeam ->
                _attemptingEntityMap.put(awayTeam.concatenateAwayTeam(), awayTeam));

        // Get ships that can attempt mission
        for (PhysicalCard card : Filters.filterYourActive(player,
                Filters.ship, Filters.atLocation(missionCard.getLocation()))) {
            if (card instanceof PhysicalShipCard ship)
                if (ship.canAttemptMission(missionCard))
                    _attemptingEntityMap.put(ship.getTitle(), ship);
        }
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

        if (!_attemptingEntityWasChosen) {
            Effect chooseAwayTeamEffect = new ChooseAwayTeamEffect(player,
                    _attemptingEntityMap.keySet().stream().toList()) {
                @Override
                protected void awayTeamChosen(String result) {
                    setAttemptingUnit(_attemptingEntityMap.get(result));
                }
            };
            SubAction subAction = new SubAction(this, cardGame);
            subAction.appendEffect(chooseAwayTeamEffect);
            return getNextCost();
        }

        if (!_missionAttemptInitiated) {
            _missionAttemptInitiated = true;
            return new AllowResponsesAction(cardGame, this, EffectResult.Type.START_OF_MISSION_ATTEMPT);
        }

        List<PhysicalCard> seedCards = _missionCard.getCardsSeededUnderneath();
        if (!seedCards.isEmpty() && !_missionAttemptEnded) {
            PhysicalCard firstSeedCard = seedCards.getFirst();
            if (!_revealedCards.contains(firstSeedCard)) {
                _revealedCards.add(firstSeedCard);
                return new RevealSeedCardAction(cardGame.getPlayer(_performingPlayerId), firstSeedCard, _missionCard);
            }
            if (!_encounteredCards.contains(firstSeedCard)) {
                _encounteredCards.add(firstSeedCard);
//                return new EncounterSeedCardAction(_performingPlayerId, firstSeedCard);
            }
        }

        if (seedCards.isEmpty() && !_missionAttemptEnded) {
//            return new SolveMissionAction(_missionCard);
            ST1EGameState gameState = (ST1EGameState) cardGame.getGameState();
            if (cardGame.getModifiersQuerying().canPlayerSolveMission(_performingPlayerId, _missionCard)) {
                MissionRequirement requirement = _missionCard.getRequirements();
                if (requirement.canBeMetBy(_attemptingUnit.getAttemptingPersonnel())) {
                    cardGame.sendMessage("DEBUG - Mission solved!");
                    cardGame.getGameState().getPlayer(_performingPlayerId).scorePoints(_missionCard.getPoints());
                    _missionCard.setCompleted(true);
                    gameState.checkVictoryConditions();
                } else cardGame.sendMessage("DEBUG - Mission attempt failed!");
            }
            _missionAttemptEnded = true;
        }

        return getNextAction();
    }

    public PhysicalCard getMission() { return _missionCard; }

    public void setAttemptingUnit(AttemptingUnit attemptingUnit) {
        _attemptingUnit = attemptingUnit;
        _attemptingEntityWasChosen = true;
    }

}