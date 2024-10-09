package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.ConstantValueSource;
import com.gempukku.stccg.cards.ValueSource;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Keyword;
import com.gempukku.stccg.condition.Condition;

public class KeywordModifier extends AbstractModifier implements KeywordAffectingModifier {
    private final Keyword _keyword;
    private final ValueSource _valueSource;
    private final ActionContext _context;

    public KeywordModifier(ActionContext context, Filterable affectFilter, Keyword keyword, int count) {
        this(context, affectFilter, null, keyword, new ConstantValueSource(count));
    }

    public KeywordModifier(ActionContext context, Filterable affectFilter, Condition condition, Keyword keyword,
                           ValueSource evaluator) {
        super(context.getSource(), null, affectFilter, condition, ModifierEffect.GIVE_KEYWORD_MODIFIER);
        _context = context;
        _keyword = keyword;
        _valueSource = evaluator;
    }

    @Override
    public Keyword getKeyword() {
        return _keyword;
    }

    @Override
    public String getCardInfoText(PhysicalCard affectedCard) {
        if (_keyword.isMultiples()) {
            int count = _valueSource.evaluateExpression(_context, affectedCard);
            return _keyword.getHumanReadable() + " +" + count;
        }
        return _keyword.getHumanReadable();
    }

    @Override
    public boolean hasKeyword(PhysicalCard physicalCard, Keyword keyword) {
        return (keyword == _keyword && _valueSource.evaluateExpression(_context, physicalCard) > 0);
    }

    @Override
    public int getKeywordCountModifier(PhysicalCard physicalCard, Keyword keyword) {
        if (keyword == _keyword)
            return _valueSource.evaluateExpression(_context, physicalCard);
        else
            return 0;
    }
}
