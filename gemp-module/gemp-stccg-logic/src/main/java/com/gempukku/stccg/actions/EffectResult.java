package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

import java.util.*;

public class EffectResult {
    private final Set<Action> _optionalTriggersUsed = new HashSet<>();

    public enum Type {
        ACTIVATE,
        ACTIVATE_TRIBBLE_POWER,
        DRAW_CARD_OR_PUT_INTO_HAND,
        END_OF_TURN,
        FOR_EACH_DISCARDED_FROM_DECK,
        FOR_EACH_DISCARDED_FROM_HAND,
        FOR_EACH_DISCARDED_FROM_PLAY,
        FOR_EACH_DISCARDED_FROM_PLAY_PILE,
        FOR_EACH_RETURNED_TO_HAND,
        FOR_EACH_REVEALED_FROM_HAND,
        FOR_EACH_REVEALED_FROM_TOP_OF_DECK,
        PLAY_CARD,
        PLAYER_WENT_OUT,
        START_OF_MISSION_ATTEMPT,
        START_OF_PHASE,
        START_OF_TURN,
        WHEN_MOVE_FROM
    }

    private final Type _type;
    protected String _playerId;
    private final DefaultGame _game;
    protected final PhysicalCard _source;
    private Map<Player, List<Action>> _optionalAfterTriggerActions = new HashMap<>();
        // TODO - In general this isn't doing a great job of assessing who actually performed the action
    protected final String _performingPlayerId;

    protected EffectResult(Type type, PhysicalCard source) {
        _type = type;
        _game = source.getGame();
        _source = source;
        _performingPlayerId = source.getOwnerName();
    }

    public EffectResult(Type type, DefaultGame game) {
        _type = type;
        _game = game;
        _source = null;
        _performingPlayerId = null;
    }

    protected EffectResult(Type type, Effect effect) {
        _type = type;
        _game = effect.getGame();
        _source = effect.getSource();
        _performingPlayerId = effect.getPerformingPlayerId();
    }

    protected EffectResult(Type type, Effect effect, PhysicalCard source) {
        _type = type;
        _game = effect.getGame();
        _source = source;
        _performingPlayerId = effect.getPerformingPlayerId();
    }


    public Type getType() {
        return _type;
    }
    public void optionalTriggerUsed(Action action) {
        _optionalTriggersUsed.add(action);
    }
    public boolean wasOptionalTriggerUsed(Action action) {
        return _optionalTriggersUsed.contains(action);
    }
    public String getPlayer() { return _playerId; }

    public List<Action> getOptionalAfterTriggerActions(Player player) {
        if (_optionalAfterTriggerActions.get(player) == null)
            return new LinkedList<>();
        else return _optionalAfterTriggerActions.get(player);
    }

    public void createOptionalAfterTriggerActions() {
        Map<Player, List<Action>> allActions = new HashMap<>();
        for (Player player : _game.getPlayers()) {
            List<Action> playerActions = new LinkedList<>();
            for (PhysicalCard card : Filters.filterYourActive(player)) {
                if (!card.hasTextRemoved(_game)) {
                    final List<Action> actions = card.getOptionalAfterTriggerActions(player.getPlayerId(), this);
                    if (actions != null)
                        playerActions.addAll(actions);
                }
            }
            allActions.put(player, playerActions);
        }
        _optionalAfterTriggerActions = allActions;
    }

    public String getPerformingPlayerId() { return _performingPlayerId; }
    public DefaultGame getGame() { return _game; }
}