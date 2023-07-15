package com.gempukku.lotro.game.modifiers.lotronly;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.game.modifiers.AbstractModifier;
import com.gempukku.lotro.game.modifiers.Condition;
import com.gempukku.lotro.game.modifiers.ModifierEffect;

public class AllyParticipatesInSkirmishesModifier extends AbstractModifier {
    private final PhysicalCard _source;

    public AllyParticipatesInSkirmishesModifier(PhysicalCard source, Filterable... affectFilters) {
        this(source, null, affectFilters);
    }

    public AllyParticipatesInSkirmishesModifier(PhysicalCard source, Condition condition, Filterable... affectFilters) {
        super(source, "Can participate in skirmishes", Filters.and(affectFilters), condition, ModifierEffect.PRESENCE_MODIFIER);
        _source = source;
    }

    @Override
    public boolean isAllyParticipateInSkirmishes(DefaultGame game, Side sidePlayer, PhysicalCard card) {
        boolean unhasty = game.getModifiersQuerying().hasKeyword(game, card, Keyword.UNHASTY);
        return sidePlayer == Side.SHADOW
                || !unhasty || _source.getBlueprint().getCulture() == Culture.GANDALF;
    }
}