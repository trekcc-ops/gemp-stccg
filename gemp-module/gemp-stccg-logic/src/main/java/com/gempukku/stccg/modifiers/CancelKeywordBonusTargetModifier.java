package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Keyword;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.condition.Condition;

public class CancelKeywordBonusTargetModifier extends AbstractModifier implements KeywordAffectingModifier {
    private final Keyword _keyword;
    private final Filter _sourceFilter;

    public CancelKeywordBonusTargetModifier(PhysicalCard source, Keyword keyword, Condition condition, Filterable affectFilter, Filterable sourceFilter) {
        super(source, "Cancel " + keyword.getHumanReadable() + " keyword", affectFilter, condition, ModifierEffect.CANCEL_KEYWORD_BONUS_TARGET_MODIFIER);
        _keyword = keyword;
        _sourceFilter = Filters.and(sourceFilter);
    }

    @Override
    public boolean appliesKeywordModifier(DefaultGame game, PhysicalCard modifierSource, Keyword keyword) {
        return keyword != _keyword
                || (_sourceFilter != null && (modifierSource == null || !_sourceFilter.accepts(game, modifierSource)));
    }

    @Override
    public Keyword getKeyword() {
        return _keyword;
    }
}