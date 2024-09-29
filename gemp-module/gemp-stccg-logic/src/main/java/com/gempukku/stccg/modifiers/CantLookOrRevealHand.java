package com.gempukku.stccg.modifiers;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.game.DefaultGame;

public class CantLookOrRevealHand implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JsonNode node, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        environment.validateAllowedFields(node, "player", "hand");

        PlayerSource playerSource = PlayerResolver.resolvePlayer(node.get("player").textValue());
        PlayerSource handSource = PlayerResolver.resolvePlayer(node.get("hand").textValue());

        return actionContext -> new AbstractModifier(actionContext.getSource(),
                "Player may not look at or reveal cards in another player hand",
                null, ModifierEffect.LOOK_OR_REVEAL_MODIFIER) {
            @Override
            public boolean canLookOrRevealCardsInHand(DefaultGame game, String revealingPlayerId, String actingPlayerId) {
                return !playerSource.getPlayerId(actionContext).equals(actingPlayerId)
                        || !handSource.getPlayerId(actionContext).equals(revealingPlayerId);
            }
        };
    }
}
