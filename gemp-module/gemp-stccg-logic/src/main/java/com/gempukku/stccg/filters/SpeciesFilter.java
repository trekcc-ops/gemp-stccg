package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Species;
import com.gempukku.stccg.game.DefaultGame;

public class SpeciesFilter implements CardFilter {

    private final Species _species;

    public SpeciesFilter(Species species) {
        _species = species;
    }
    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return physicalCard.getBlueprint().isSpecies(_species);
    }
}