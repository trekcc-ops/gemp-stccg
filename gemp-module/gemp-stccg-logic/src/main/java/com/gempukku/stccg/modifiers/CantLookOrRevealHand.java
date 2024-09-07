package com.gempukku.stccg.modifiers;

import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.game.DefaultGame;
import org.json.simple.JSONObject;

public class CantLookOrRevealHand implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardBlueprintFactory environment) throws InvalidCardDefinitionException {
        environment.validateAllowedFields(object, "player", "hand");

        final String player = environment.getString(object.get("player"), "player");
        final String hand = environment.getString(object.get("hand"), "hand");

        PlayerSource playerSource = PlayerResolver.resolvePlayer(player);
        PlayerSource handSource = PlayerResolver.resolvePlayer(hand);

        return actionContext -> new AbstractModifier(actionContext.getSource(), "Player may not look at or reveal cards in another player hand",
                null, ModifierEffect.LOOK_OR_REVEAL_MODIFIER) {
            @Override
            public boolean canLookOrRevealCardsInHand(DefaultGame game, String revealingPlayerId, String actingPlayerId) {
                return !playerSource.getPlayerId(actionContext).equals(actingPlayerId)
                        || !handSource.getPlayerId(actionContext).equals(revealingPlayerId);
            }
        };
    }
}
