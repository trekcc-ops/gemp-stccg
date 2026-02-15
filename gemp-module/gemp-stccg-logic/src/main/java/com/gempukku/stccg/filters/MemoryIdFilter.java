package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class MemoryIdFilter implements CardFilter {

    private final ActionContext _context;

    public MemoryIdFilter(ActionContext context) {
        _context = context;
    }
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return false;
    }
}