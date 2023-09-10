package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.actions.lotronly.CostToEffectAction;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.DefaultActionContext;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.PlayerSource;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.resolver.PlayerResolver;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.effects.PlaceTopCardOfDrawDeckOnTopOfPlayPileEffect;
import com.gempukku.lotro.game.DefaultGame;
import org.json.simple.JSONObject;

public class PlaceTopCardOfDrawDeckOnTopOfPlayPile implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "player");

        final String player = FieldUtils.getString(effectObject.get("player"), "player", "you");

        final PlayerSource playerSource = PlayerResolver.resolvePlayer(player);

        return new DelayedAppender<>() {
            @Override
            public boolean isPlayableInFull(DefaultActionContext<DefaultGame> actionContext) {
                final String drawPlayer = playerSource.getPlayer(actionContext);
                return actionContext.getGame().getGameState().getDeck(drawPlayer).size() >= 1;
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                final String drawPlayer = playerSource.getPlayer(actionContext);
                return new PlaceTopCardOfDrawDeckOnTopOfPlayPileEffect(drawPlayer);
            }
        };
    }

}
