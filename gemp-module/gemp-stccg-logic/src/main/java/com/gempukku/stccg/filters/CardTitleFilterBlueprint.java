package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;

public class CardTitleFilterBlueprint implements FilterBlueprint {

    private final String _cardTitle;

    public CardTitleFilterBlueprint(String cardTitle) {
        _cardTitle = cardTitle;
    }

    @Override
    public CardFilter getFilterable(DefaultGame cardGame, ActionContext actionContext) {
        return Filters.name(_cardTitle);
    }
}