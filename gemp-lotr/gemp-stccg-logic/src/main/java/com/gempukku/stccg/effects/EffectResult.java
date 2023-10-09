package com.gempukku.stccg.effects;

import com.gempukku.stccg.actions.OptionalTriggerAction;

import java.util.HashSet;
import java.util.Set;

public abstract class EffectResult {
    private final Set<String> _optionalTriggersUsed = new HashSet<>();

    public enum Type {
        // May be relevant to multiple games
        ACTIVATE, ANY_NUMBER_KILLED, CARD_TRANSFERRED, END_OF_PHASE, END_OF_TURN,
        FOR_EACH_DISCARDED_FROM_DECK, FOR_EACH_DISCARDED_FROM_HAND, FOR_EACH_DISCARDED_FROM_PLAY, FOR_EACH_KILLED,
        FOR_EACH_RETURNED_TO_HAND, FOR_EACH_REVEALED_FROM_HAND, FOR_EACH_REVEALED_FROM_TOP_OF_DECK, PLAY,
        START_OF_PHASE, START_OF_TURN, WHEN_MOVE_FROM, WHEN_MOVE_TO,

        // Tribbles-specific
        ACTIVATE_TRIBBLE_POWER,  DRAW_CARD_OR_PUT_INTO_HAND, FOR_EACH_DISCARDED_FROM_PLAY_PILE,
        PLAYER_WENT_OUT,

        // LotR-specific
        AFTER_ALL_SKIRMISHES, ASSIGNED_AGAINST, ASSIGNED_TO_SKIRMISH, CHARACTER_WON_SKIRMISH, CHARACTER_LOST_SKIRMISH,
        FINISHED_PLAYING_FELLOWSHIP, FOR_EACH_EXERTED, FOR_EACH_HEALED,
        FREE_PEOPLE_PLAYER_STARTS_ASSIGNING, FREE_PEOPLE_PLAYER_DECIDED_IF_MOVING, INITIATIVE_CHANGE,
        PUT_ON_THE_ONE_RING, RECONCILE, REMOVE_BURDEN, REPLACE_SITE,
        SKIRMISH_ABOUT_TO_END, SKIRMISH_CANCELLED, SKIRMISH_FINISHED_WITH_OVERWHELM, SKIRMISH_FINISHED_NORMALLY,
        TAKE_CONTROL_OF_SITE, THREAT_WOUND_TRIGGER, WHEN_FELLOWSHIP_MOVES, ZERO_VITALITY
    }

    private final Type _type;
    protected String _playerId;

    protected EffectResult(Type type) {
        _type = type;
    }

    public Type getType() {
        return _type;
    }

    public void optionalTriggerUsed(OptionalTriggerAction action) {
        _optionalTriggersUsed.add(action.getTriggerIdentifier());
    }

    public boolean wasOptionalTriggerUsed(OptionalTriggerAction action) {
        return _optionalTriggersUsed.contains(action.getTriggerIdentifier());
    }

    public String getPlayer() { return _playerId; }
}
