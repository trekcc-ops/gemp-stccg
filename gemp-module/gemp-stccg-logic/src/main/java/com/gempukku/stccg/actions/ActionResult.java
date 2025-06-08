package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.*;

public class ActionResult {
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
        PLAY_CARD_INITIATION,
        PLAYER_WENT_OUT,
        START_OF_MISSION_ATTEMPT,
        START_OF_PHASE,
        START_OF_TURN,
        DRAW_CARD, KILL_CARD, WHEN_MOVE_FROM
    }

    private final Type _type;
    private Map<Player, List<TopLevelSelectableAction>> _optionalAfterTriggerActions = new HashMap<>();
        // TODO - In general this isn't doing a great job of assessing who actually performed the action
    protected final String _performingPlayerId;

    public ActionResult(Type type, String performingPlayerId) {
        _type = type;
        _performingPlayerId = performingPlayerId;
    }


    public ActionResult(Type type, Action action) {
        _type = type;
        _performingPlayerId = action.getPerformingPlayerId();
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

    public List<TopLevelSelectableAction> getOptionalAfterTriggerActions(Player player) {
        if (_optionalAfterTriggerActions.get(player) == null)
            return new LinkedList<>();
        else return _optionalAfterTriggerActions.get(player);
    }

    public void createOptionalAfterTriggerActions(DefaultGame game) throws PlayerNotFoundException {
        Map<Player, List<TopLevelSelectableAction>> allActions = new HashMap<>();
        for (Player player : game.getPlayers()) {
            List<TopLevelSelectableAction> playerActions = new LinkedList<>();
            for (PhysicalCard card : Filters.filterActive(game)) { // cards in play
                if (!card.hasTextRemoved(game)) {
                    final List<TopLevelSelectableAction> actions =
                            card.getOptionalAfterTriggerActions(player, this);
                    if (actions != null)
                        playerActions.addAll(actions);
                }
            }
            for (PhysicalCard card : player.getCardsInHand()) {
                final List<TopLevelSelectableAction> actions = card.getOptionalResponseActionsWhileInHand(player, this);
                if (actions != null)
                    playerActions.addAll(actions);
            }
            allActions.put(player, playerActions);
        }
        _optionalAfterTriggerActions = allActions;
    }


    public String getPerformingPlayerId() { return _performingPlayerId; }
}