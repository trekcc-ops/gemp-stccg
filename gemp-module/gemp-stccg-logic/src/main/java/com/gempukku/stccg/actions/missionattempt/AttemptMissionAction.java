package com.gempukku.stccg.actions.missionattempt;

import com.gempukku.stccg.actions.AbstractCostToEffectAction;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.StackActionEffect;
import com.gempukku.stccg.actions.choose.ChooseAwayTeamEffect;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.AwayTeam;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.modifiers.ModifiersQuerying;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class AttemptMissionAction extends AbstractCostToEffectAction {
    private AttemptingUnit _attemptingUnit;
    private final Player _player;
    private final ST1EGame _game;
    private final MissionCard _missionCard;
    private Effect _attemptMissionEffect;
    private boolean _attemptingEntityWasChosen, _missionAttemptInitiated, _missionAttemptEnded;
    private final Effect _chooseAwayTeamEffect;
    final Map<String, AttemptingUnit> _attemptingEntityMap = new HashMap<>();
    List<String> _seedCards;

    public AttemptMissionAction(Player player, MissionCard missionCard) {
        super(player, ActionType.ATTEMPT_MISSION);
        _player = player;
        _game = missionCard.getGame();
        _missionCard = missionCard;
        Action _thisAction = this;

        final GameState gameState = _game.getGameState();
        final ModifiersQuerying modifiersQuerying = _game.getModifiersQuerying();

        // Get Away Teams that can attempt mission
        Stream<AwayTeam> awayTeamOptions = missionCard.getYourAwayTeamsOnSurface(_player).filter(
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

        // Choose Away Team to attempt mission
        _chooseAwayTeamEffect = new ChooseAwayTeamEffect(_game, _performingPlayerId, _attemptingEntityMap.keySet().stream().toList()) {
            @Override
            protected void awayTeamChosen(String result) {
                _attemptingEntityWasChosen = true;
                _attemptingUnit = _attemptingEntityMap.get(result);
                _attemptMissionEffect = new AttemptMissionEffect(_player, _attemptingUnit, _missionCard);
            }
        };
    }

    @Override
    public String getText() { return "Attempt mission"; }

    @Override
    public PhysicalCard getActionAttachedToCard() { return _missionCard; }
    @Override
    public PhysicalCard getActionSource() { return _missionCard; }

    @Override
    public boolean canBeInitiated() {
        return _missionCard.mayBeAttemptedByPlayer(_player);
    }

    @Override
    public Effect nextEffect() throws InvalidGameLogicException {

        Effect cost = getNextCost();
        if (cost != null)
            return cost;

        if (!_attemptingEntityWasChosen) {
            appendTargeting(_chooseAwayTeamEffect);
            return getNextCost();
        }

        if (!_missionAttemptInitiated) {
            _missionAttemptInitiated = true;
                // DEBUG lines of dialog
            _game.sendMessage("Mission attempt initiated. This is when you would ordinarily encounter dilemmas and stuff like that.");
            _game.sendMessage("...");
            _game.sendMessage("But we don't have any.");
            _seedCards = new LinkedList<>();        // TODO - Replace this with real stuff at some point
/*            _seedCards.add("Armus - Skin of Evil");
            _seedCards.add("Berserk Changeling");
            _seedCards.add("Nanites"); */

        }

        if (!_seedCards.isEmpty()) {
            return new StackActionEffect(_game, new EncounterSeedCardAction(this, _seedCards));
        }

        if (!_missionAttemptEnded) {
            _missionAttemptEnded = true;
            return _attemptMissionEffect;
        }

        return getNextEffect();
    }

    public boolean wasActionCarriedOut() {
        return _missionAttemptInitiated;
    }

    @Override
    public ST1EGame getGame() { return _game; }

    public Player getPlayer() { return _player; }
    public AttemptingUnit getAttemptingEntity() { return _attemptingUnit; }

}
