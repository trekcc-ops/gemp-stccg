package com.gempukku.stccg.cards.blueprints.requirement;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.FilterFactory;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.evaluator.ValueSource;
import com.gempukku.stccg.evaluator.ValueResolver;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.PlayerNotFoundException;
import com.gempukku.stccg.gamestate.GameState;

public class MiscRequirement implements Requirement {

    private enum RequirementType {
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
    private final FilterableSource _filterableSource;

    public MiscRequirement(JsonNode node) throws InvalidCardDefinitionException {
        _requirementType = BlueprintUtils.getEnum(RequirementType.class, node, "type", false);
        if (_requirementType == null)
            throw new InvalidCardDefinitionException("Invalid requirement type");

        switch (_requirementType) {
            case CARDSINDECKCOUNT, CARDSINHANDMORETHAN:
                BlueprintUtils.validateAllowedFields(node, "count");
                BlueprintUtils.validateRequiredFields(node, "count");
                break;
            case HASCARDINDISCARD, HASCARDINHAND, HASCARDINPLAYPILE:
                BlueprintUtils.validateAllowedFields(node, "count", "filter");
                BlueprintUtils.validateRequiredFields(node, "filter");
                break;
        }

        _playerSource = BlueprintUtils.getTargetPlayerSource(node);
        _valueSource = ValueResolver.resolveEvaluator(node.get("count"), 1);
        _filterableSource = (node.has("filter")) ?
                new FilterFactory().generateFilter(node.get("filter")) : actionContext -> Filters.any;
    }

    public boolean accepts(ActionContext actionContext) {

        try {
            final String playerId = _playerSource.getPlayerId(actionContext);
            Player player = actionContext.getGame().getPlayer(playerId);
            final int count = _valueSource.evaluateExpression(actionContext, null);
            final GameState gameState = actionContext.getGameState();
            final Filterable filterable = _filterableSource.getFilterable(actionContext);
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