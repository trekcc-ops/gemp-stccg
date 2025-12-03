package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.player.*;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.AnyCardFilterBlueprint;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Objects;

public class CountDiscardValueSource extends ValueSource {

    private final int _multiplier;
    private final int _limit;
    private final PlayerSource _playerSource;
    private final FilterBlueprint _filterBlueprint;

    public CountDiscardValueSource(@JsonProperty("filter")
                                 FilterBlueprint filterBlueprint,
                                   @JsonProperty("player")
                                 String playerText,
                                   @JsonProperty("limit")
                                 Integer limit,
                                   @JsonProperty("multiplier")
                                 Integer multiplier) throws InvalidCardDefinitionException {
        _multiplier = Objects.requireNonNullElse(multiplier, 1);
        _limit = Objects.requireNonNullElse(limit, Integer.MAX_VALUE);
        _playerSource = (playerText == null) ?
                new YouPlayerSource() : PlayerResolver.resolvePlayer(playerText);
        _filterBlueprint = Objects.requireNonNullElse(filterBlueprint, new AnyCardFilterBlueprint());
    }

    @Override
    public float evaluateExpression(DefaultGame cardGame, ActionContext actionContext) {
        try {
            String playerId = _playerSource.getPlayerId(cardGame, actionContext);
            Player player = cardGame.getPlayer(playerId);
            final Filterable filterable = _filterBlueprint.getFilterable(cardGame, actionContext);
            int count = Filters.filter(player.getCardGroupCards(Zone.DISCARD), cardGame, filterable).size();
            return Math.min(_limit, count);
        } catch(PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            return 0;
        }
    }
}