package com.gempukku.stccg.gameevent;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.player.Player;

public class StartOfPhaseGameEvent extends GameEvent {

    @JacksonXmlProperty(localName = "phase", isAttribute = true)
    private final Phase _phase;

    public StartOfPhaseGameEvent(Phase phase) {
        super(Type.GAME_PHASE_CHANGE);
        _phase = phase;
        _eventAttributes.put(Attribute.phase, phase.name());
    }

}