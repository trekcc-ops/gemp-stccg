package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Objects;

public class TitleFilter implements CardFilter {

    private final String _title;
    public TitleFilter(String title) {
        _title = title;
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return Objects.equals(physicalCard.getBlueprint().getTitle(), _title);
    }
}