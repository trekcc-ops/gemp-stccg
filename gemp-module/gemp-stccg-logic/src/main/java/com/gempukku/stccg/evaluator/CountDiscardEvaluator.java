package com.gempukku.stccg.evaluator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.player.PlayerResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.Objects;

public class CountDiscardEvaluator implements ValueSource {

    private final int _multiplier;
    private final int _limit;
    private final PlayerSource _playerSource;
    private final FilterBlueprint _filterBlueprint;

    public CountDiscardEvaluator(@JsonProperty("filter")
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
                ActionContext::getPerformingPlayerId : PlayerResolver.resolvePlayer(playerText);
        _filterBlueprint = Objects.requireNonNullElse(filterBlueprint, actionContext -> Filters.any);
    }

    @Override
    public Evaluator getEvaluator(ActionContext actionContext) {
        return new MultiplyEvaluator(actionContext, _multiplier, new Evaluator() {
            final String playerId = _playerSource.getPlayerId(actionContext);
            @Override
            public int evaluateExpression(DefaultGame game) {
                try {
                    Player player = game.getPlayer(playerId);
                    final Filterable filterable = _filterBlueprint.getFilterable(actionContext);
                    int count = Filters.filter(player.getCardGroupCards(Zone.DISCARD), game, filterable).size();
                    return Math.min(_limit, count);
                } catch(PlayerNotFoundException exp) {
                    game.sendErrorMessage(exp);
                    return 0;
                }
            }
        });
    }
}