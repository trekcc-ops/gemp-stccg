package com.gempukku.stccg.actions.lotr;

import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.playcard.PlayCardResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.actions.lotr.PlayEventAction;

public class PlayEventResult extends PlayCardResult {
    private boolean _eventCancelled;
    private final PlayEventAction _action;
    private final boolean _requiresRanger;

    public PlayEventResult(Effect effect, PlayEventAction action, Zone playedFrom,
                           PhysicalCard playedCard, boolean requiresRanger) {
        super(effect, playedFrom, playedCard, null, null);
        _action = action;
        _requiresRanger = requiresRanger;
    }

    public boolean isRequiresRanger() {
        return _requiresRanger;
    }

    public void cancelEvent() {
        _eventCancelled = true;
    }

    public boolean isEventNotCancelled() {
        return !_eventCancelled;
    }

    public PlayEventAction getPlayEventAction() {
        return _action;
    }
}
