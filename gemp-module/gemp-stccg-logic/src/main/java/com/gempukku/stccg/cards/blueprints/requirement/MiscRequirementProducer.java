package com.gempukku.stccg.cards.blueprints.requirement;

import com.fasterxml.jackson.databind.JsonNode;
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

public class MiscRequirementProducer extends RequirementProducer {

    private enum RequirementType {
        CARDSINDECKCOUNT(Zone.DRAW_DECK),
        CARDSINHANDMORETHAN(Zone.HAND),
        HASCARDINDISCARD(Zone.DISCARD),
        HASCARDINHAND(Zone.HAND),
        HASCARDINPLAYPILE(Zone.PLAY_PILE),
        HASINZONEDATA(null),
        NEXTTRIBBLEINSEQUENCE(null);

        private Zone zone;
        RequirementType(Zone zone) { this.zone = zone; }
    }
    @Override
    public Requirement getPlayRequirement(JsonNode node) throws InvalidCardDefinitionException {
        RequirementType requirementType = BlueprintUtils.getEnum(RequirementType.class, node, "type");

        switch(requirementType) {
            case CARDSINDECKCOUNT, CARDSINHANDMORETHAN, NEXTTRIBBLEINSEQUENCE:
                BlueprintUtils.validateAllowedFields(node, "count");
                BlueprintUtils.validateRequiredFields(node, "count");
                break;
            case HASCARDINDISCARD, HASCARDINHAND, HASCARDINPLAYPILE:
                BlueprintUtils.validateAllowedFields(node,"count", "filter");
                BlueprintUtils.validateRequiredFields(node, "filter");
                break;
            case HASINZONEDATA:
                BlueprintUtils.validateAllowedFields(node,"filter");
                BlueprintUtils.validateRequiredFields(node, "filter");
                break;
        }

        final PlayerSource playerSource = BlueprintUtils.getTargetPlayerSource(node);
        final ValueSource valueSource = ValueResolver.resolveEvaluator(node.get("count"), 1);
        final FilterableSource filterableSource = (node.has("filter")) ?
                new FilterFactory().generateFilter(node.get("filter")) : actionContext -> Filters.any;

        return actionContext -> {
            final String playerId = playerSource.getPlayerId(actionContext);
            final int count = valueSource.evaluateExpression(actionContext, null);
            final GameState gameState = actionContext.getGameState();
            final Filterable filterable = filterableSource.getFilterable(actionContext);
            return switch(requirementType) {
                case CARDSINDECKCOUNT -> gameState.getDrawDeck(playerId).size() == count;
                case CARDSINHANDMORETHAN -> gameState.getHand(playerId).size() > count;
                case HASCARDINDISCARD, HASCARDINHAND, HASCARDINPLAYPILE ->
                        gameState.getPlayer(playerId).hasCardInZone(requirementType.zone, count, filterable);
                case HASINZONEDATA -> {
                    for (PhysicalCard card : Filters.filterActive(actionContext.getGame(), filterable)) {
                        if (card.getWhileInZoneData() != null)
                            yield true;
                    }
                    yield false;
                }
                case NEXTTRIBBLEINSEQUENCE ->
                        actionContext instanceof TribblesActionContext context &&
                                context.getGameState().getNextTribbleInSequence() == count;
            };
        };
    }
}