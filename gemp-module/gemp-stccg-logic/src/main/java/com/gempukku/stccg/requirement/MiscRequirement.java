package com.gempukku.stccg.requirement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.evaluator.ConstantEvaluator;
import com.gempukku.stccg.evaluator.ValueSource;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.gamestate.GameState;

import java.util.Objects;

public class MiscRequirement implements Requirement {

    protected enum RequirementType {
        CARDSINDECKCOUNT(Zone.DRAW_DECK),
        CARDSINHANDMORETHAN(Zone.HAND),
        HASCARDINDISCARD(Zone.DISCARD),
        HASCARDINHAND(Zone.HAND),
        HASCARDINPLAYPILE(Zone.PLAY_PILE);

        private final Zone zone;
        RequirementType(Zone zone) { this.zone = zone; }
    }

    private final RequirementType _requirementType;
    private final PlayerSource _playerSource;
    private final ValueSource _valueSource;
    private final FilterBlueprint _filterBlueprint;

    public MiscRequirement(@JsonProperty("type")
                           RequirementType requirementType,
                           @JsonProperty(value = "count", required = true)
                           ValueSource count,
                           @JsonProperty(value="filter", required = true)
                           FilterBlueprint filterBlueprint) {
        _requirementType = requirementType;
        _playerSource = ActionContext::getPerformingPlayerId;
        _valueSource = Objects.requireNonNullElse(count, new ConstantEvaluator(1));
        _filterBlueprint = Objects.requireNonNullElse(filterBlueprint, actionContext -> Filters.any);
    }

    public boolean accepts(ActionContext actionContext) {

        try {
            final String playerId = _playerSource.getPlayerId(actionContext);
            Player player = actionContext.getGame().getPlayer(playerId);
            final int count = (int) _valueSource.evaluateExpression(actionContext, null);
            final GameState gameState = actionContext.getGameState();
            final Filterable filterable = _filterBlueprint.getFilterable(actionContext);
            return switch (_requirementType) {
                case CARDSINDECKCOUNT -> player.getCardsInDrawDeck().size() == count;
                case CARDSINHANDMORETHAN -> player.getCardsInHand().size() > count;
                case HASCARDINDISCARD, HASCARDINHAND, HASCARDINPLAYPILE ->
                        gameState.getPlayer(playerId).hasCardInZone(
                                actionContext.getGame(), _requirementType.zone, count, filterable);
            };
        } catch(PlayerNotFoundException exp) {
            actionContext.getGame().sendErrorMessage(exp);
            return false;
        }
    }
}