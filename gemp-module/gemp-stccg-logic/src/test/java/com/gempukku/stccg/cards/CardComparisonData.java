package com.gempukku.stccg.cards;

import com.gempukku.stccg.cards.blueprints.CardBlueprint;

import java.util.Objects;

public class CardComparisonData {

    private final CardBlueprint _blueprint;
    private final CardData _lackeyData;

    public CardComparisonData(CardBlueprint blueprint, CardData lackeyData) {
        _blueprint = blueprint;
        _lackeyData = lackeyData;
    }

    public boolean matchesLore() {
        String blueprintLore = Objects.requireNonNullElse(_blueprint.getLore(),"")
                .replace("<i>", "").replace("</i>", "")
                .replace("<b>", "").replace("</b>", "");
        String lackeyLore = _lackeyData._lore;
        return Objects.equals(blueprintLore, lackeyLore);
    }

}