package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.cards.PhysicalMissionCard;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.choose.ChooseAwayTeamEffect;
import com.gempukku.stccg.effects.defaulteffect.AttemptMissionEffect;
import com.gempukku.stccg.game.AwayTeam;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.modifiers.ModifiersQuerying;
import com.gempukku.stccg.rules.GameUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class AttemptMissionAction extends AbstractCostToEffectAction {
    private AwayTeam _awayTeam;
    private final String _playerId;
    private final Player _player;
    private final ST1EGame _game;
    private final PhysicalMissionCard _missionCard;
    private Effect _attemptMissionEffect;
    private boolean _awayTeamChosen, _missionAttemptInitiated;
    private final Effect _chooseAwayTeamEffect;
    Map<String, AwayTeam> _awayTeamMapping = new HashMap<>();

    public AttemptMissionAction(Player player, PhysicalMissionCard missionCard) {
        _player = player;
        _game = missionCard.getGame();
        _playerId = player.getPlayerId();
        _missionCard = missionCard;
        Action _thisAction = this;

        final GameState gameState = _game.getGameState();
        final ModifiersQuerying modifiersQuerying = _game.getModifiersQuerying();

        // Get Away Teams that can attempt mission
        Stream<AwayTeam> awayTeamOptions = missionCard.getYourAwayTeamsOnSurface(_player).filter(
                awayTeam -> awayTeam.canAttemptMission(missionCard));
        awayTeamOptions.forEach(awayTeam ->
                _awayTeamMapping.put(GameUtils.concatenateStrings(awayTeam.getListOfAwayTeamCards()), awayTeam));

        // Choose Away Team to attempt mission
        _chooseAwayTeamEffect = new ChooseAwayTeamEffect(_game, _playerId, _awayTeamMapping.keySet().stream().toList()) {
            @Override
            protected void awayTeamChosen(String result) {
                _awayTeamChosen = true;
                _awayTeam = _awayTeamMapping.get(result);
                _attemptMissionEffect = new AttemptMissionEffect(_player, _awayTeam, _missionCard);
            }
        };
    }

    @Override
    public String getText() { return "Attempt mission"; }

    @Override
    public ActionType getActionType() {
        return ActionType.ATTEMPT_MISSION;
    }

    @Override
    public PhysicalCard getActionAttachedToCard() { return _missionCard; }
    @Override
    public PhysicalCard getActionSource() { return _missionCard; }

    @Override
    public boolean canBeInitiated() {
        return _missionCard.mayBeAttemptedByPlayer(_player);
    }

    @Override
    public Effect nextEffect() {

        Effect cost = getNextCost();
        if (cost != null)
            return cost;

        if (!_awayTeamChosen) {
            appendTargeting(_chooseAwayTeamEffect);
            return getNextCost();
        }

        if (!_missionAttemptInitiated) {
            _missionAttemptInitiated = true;
            return _attemptMissionEffect;
        }

        return getNextEffect();
    }

    public boolean wasActionCarriedOut() {
        return _missionAttemptInitiated;
    }

    @Override
    public ST1EGame getGame() { return _game; }

}
