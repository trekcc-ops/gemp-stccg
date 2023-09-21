package com.gempukku.lotro.modifiers;

import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.ModifierSource;
import com.gempukku.lotro.cards.PlayerSource;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effectappender.resolver.PlayerResolver;
import com.gempukku.lotro.game.DefaultGame;
import org.json.simple.JSONObject;

public class CantLookOrRevealHand implements ModifierSourceProducer {
    @Override
    public ModifierSource getModifierSource(JSONObject object, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(object, "player", "hand");

        final String player = FieldUtils.getString(object.get("player"), "player");
        final String hand = FieldUtils.getString(object.get("hand"), "hand");

        PlayerSource playerSource = PlayerResolver.resolvePlayer(player);
        PlayerSource handSource = PlayerResolver.resolvePlayer(hand);

        return actionContext -> new AbstractModifier(actionContext.getSource(), "Player may not look at or reveal cards in another player hand",
                null, ModifierEffect.LOOK_OR_REVEAL_MODIFIER) {
            @Override
            public boolean canLookOrRevealCardsInHand(DefaultGame game, String revealingPlayerId, String actingPlayerId) {
                return !playerSource.getPlayer(actionContext).equals(actingPlayerId)
                        || !handSource.getPlayer(actionContext).equals(revealingPlayerId);
            }
        };
    }
}
