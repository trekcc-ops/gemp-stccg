package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.cards.PhysicalMissionCard;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.effects.choose.ChooseAwayTeamEffect;
import com.gempukku.stccg.effects.defaulteffect.AttemptMissionEffect;
import com.gempukku.stccg.game.AwayTeam;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
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
    private final PhysicalMissionCard _missionCard;
    private final Action _thisAction;
    private Effect _attemptMissionEffect;
    private boolean _awayTeamChosen, _missionAttemptInitiated;
    private final Effect _chooseAwayTeamEffect;
    Map<String, AwayTeam> _awayTeamMapping = new HashMap<>();

    public AttemptMissionAction(Player player, PhysicalMissionCard missionCard) {
        _player = player;
        _playerId = player.getPlayerId();
        _missionCard = missionCard;
        _thisAction = this;
        DefaultGame game = player.getGame();

        final GameState gameState = game.getGameState();
        final ModifiersQuerying modifiersQuerying = game.getModifiersQuerying();

        // Get Away Teams that can attempt mission
        Stream<AwayTeam> awayTeamOptions = missionCard.getYourAwayTeamsOnSurface(_player).filter(
                awayTeam -> awayTeam.canAttemptMission(missionCard));
        awayTeamOptions.forEach(awayTeam -> {
            String awayTeamName = GameUtils.getAppendedTextNames(awayTeam.getCards());
            _awayTeamMapping.put(awayTeamName, awayTeam);
        });

        // Choose Away Team to attempt mission
        _chooseAwayTeamEffect = new ChooseAwayTeamEffect(game, _playerId, _awayTeamMapping.keySet().stream().toList()) {
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
    public Effect nextEffect(DefaultGame game) {

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

}
