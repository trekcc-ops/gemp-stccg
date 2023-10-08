package com.gempukku.stccg.effects.choose;

import com.gempukku.stccg.common.Filterable;
import com.gempukku.stccg.common.Token;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.ModifierFlag;

public class ChooseAndRemoveCultureTokensFromCardEffect extends ChooseActiveCardEffect {
    private final Token _token;
    private final int _count;

    public ChooseAndRemoveCultureTokensFromCardEffect(PhysicalCard source, String playerId, Token token, int count, Filterable... filters) {
        super(source, playerId, "Choose card to remove tokens from", filters);
        _token = token;
        _count = count;
    }

    @Override
    protected Filter getExtraFilterForPlaying(DefaultGame game) {
        if (game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.CANT_TOUCH_CULTURE_TOKENS))
            return Filters.none;

        if (_token != null)
            return Filters.hasToken(_token, _count);
        else
            return Filters.hasAnyCultureTokens(_count);
    }

    @Override
    protected void cardSelected(DefaultGame game, PhysicalCard card) {
        if (_token != null)
            game.getGameState().removeTokens(card, _token, _count);
        else
            for (Token token : Token.values()) {
                if (game.getGameState().getTokenCount(card, token) > 0) {
                    game.getGameState().removeTokens(card, token, _count);
                    break;
                }
            }
    }
}
