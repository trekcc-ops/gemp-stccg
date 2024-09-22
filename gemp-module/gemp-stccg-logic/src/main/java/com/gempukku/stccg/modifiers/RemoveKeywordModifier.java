package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Keyword;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.condition.Condition;

public class RemoveKeywordModifier extends AbstractModifier implements KeywordAffectingModifier {
    private final Keyword _keyword;

    public RemoveKeywordModifier(PhysicalCard physicalCard, Filterable affectFilter, Keyword keyword) {
        this(physicalCard, affectFilter, null, keyword);
    }

    public RemoveKeywordModifier(PhysicalCard physicalCard, Filterable affectFilter, Condition condition, Keyword keyword) {
        super(physicalCard, "Loses " + keyword.getHumanReadable() + " keyword(s)", affectFilter, condition, ModifierEffect.REMOVE_KEYWORD_MODIFIER);
        _keyword = keyword;
    }

    @Override
    public Keyword getKeyword() {
        return _keyword;
    }

    @Override
    public boolean isKeywordRemoved(DefaultGame game, PhysicalCard physicalCard, Keyword keyword) {
        return _keyword == keyword;
    }
}
