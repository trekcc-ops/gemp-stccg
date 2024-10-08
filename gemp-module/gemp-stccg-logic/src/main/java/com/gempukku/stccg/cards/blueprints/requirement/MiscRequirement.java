package com.gempukku.stccg.cards.blueprints.requirement;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.cards.blueprints.BlueprintUtils;
import com.gempukku.stccg.cards.blueprints.FilterFactory;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.gamestate.GameState;

public class MiscRequirement implements Requirement {

    private enum RequirementType {
        CARDSINDECKCOUNT(Zone.DRAW_DECK),
        CARDSINHANDMORETHAN(Zone.HAND),
        HASCARDINDISCARD(Zone.DISCARD),
        HASCARDINHAND(Zone.HAND),
        HASCARDINPLAYPILE(Zone.PLAY_PILE),
        HASINZONEDATA(null),
        LASTTRIBBLEPLAYED(null),
        NEXTTRIBBLEINSEQUENCE(null),
        TRIBBLESEQUENCEBROKEN(null);

        private final Zone zone;
        RequirementType(Zone zone) { this.zone = zone; }
    }

    private final RequirementType _requirementType;
    private final PlayerSource _playerSource;
    private final ValueSource _valueSource;
    private final FilterableSource _filterableSource;


    public MiscRequirement(JsonNode node) throws InvalidCardDefinitionException {
        this._requirementType = BlueprintUtils.getEnum(RequirementType.class, node, "type", false);

        switch (_requirementType) {
            case CARDSINDECKCOUNT, CARDSINHANDMORETHAN, LASTTRIBBLEPLAYED, NEXTTRIBBLEINSEQUENCE:
                BlueprintUtils.validateAllowedFields(node, "count");
                BlueprintUtils.validateRequiredFields(node, "count");
                break;
            case HASCARDINDISCARD, HASCARDINHAND, HASCARDINPLAYPILE:
                BlueprintUtils.validateAllowedFields(node, "count", "filter");
                BlueprintUtils.validateRequiredFields(node, "filter");
                break;
            case HASINZONEDATA:
                BlueprintUtils.validateAllowedFields(node, "filter");
                BlueprintUtils.validateRequiredFields(node, "filter");
                break;
            case TRIBBLESEQUENCEBROKEN:
                BlueprintUtils.validateAllowedFields(node);
                break;
        }

        _playerSource = BlueprintUtils.getTargetPlayerSource(node);
        _valueSource = ValueResolver.resolveEvaluator(node.get("count"), 1);
        _filterableSource = (node.has("filter")) ?
                new FilterFactory().generateFilter(node.get("filter")) : actionContext -> Filters.any;
    }

    public boolean accepts(ActionContext actionContext) {

            final String playerId = _playerSource.getPlayerId(actionContext);
            final int count = _valueSource.evaluateExpression(actionContext, null);
            final GameState gameState = actionContext.getGameState();
            final Filterable filterable = _filterableSource.getFilterable(actionContext);
            return switch(_requirementType) {
                case CARDSINDECKCOUNT -> gameState.getDrawDeck(playerId).size() == count;
                case CARDSINHANDMORETHAN -> gameState.getHand(playerId).size() > count;
                case HASCARDINDISCARD, HASCARDINHAND, HASCARDINPLAYPILE ->
                        gameState.getPlayer(playerId).hasCardInZone(_requirementType.zone, count, filterable);
                case HASINZONEDATA -> {
                    for (PhysicalCard card : Filters.filterActive(actionContext.getGame(), filterable)) {
                        if (card.getWhileInZoneData() != null)
                            yield true;
                    }
                    yield false;
                }
                case LASTTRIBBLEPLAYED -> actionContext instanceof TribblesActionContext context &&
                        context.getGameState().getLastTribblePlayed() == count;
                case NEXTTRIBBLEINSEQUENCE -> actionContext instanceof TribblesActionContext context &&
                        context.getGameState().getNextTribbleInSequence() == count;
                case TRIBBLESEQUENCEBROKEN -> actionContext instanceof TribblesActionContext context &&
                        context.getGameState().isChainBroken();
            };
    }
}