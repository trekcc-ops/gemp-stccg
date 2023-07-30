package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.ActionContext;
import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.lotronly.LotroPhysicalCard;
import com.gempukku.lotro.cards.build.field.effect.EffectAppender;
import com.gempukku.lotro.cards.build.field.effect.EffectAppenderProducer;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.actions.lotronly.CostToEffectAction;
import com.gempukku.lotro.effects.TakeControlOfASiteEffect;
import com.gempukku.lotro.effects.Effect;
import org.json.simple.JSONObject;

public class TakeControlOfSite implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject);

        return new DelayedAppender() {
            @Override
            public boolean isPlayableInFull(ActionContext actionContext) {
                final DefaultGame game = actionContext.getGame();
                int maxUnoccupiedSite = Integer.MAX_VALUE;
                for (String playerId : game.getGameState().getPlayerOrder().getAllPlayers())
                    maxUnoccupiedSite = Math.min(maxUnoccupiedSite, game.getGameState().getPlayerPosition(playerId) - 1);

                for (int i = 1; i <= maxUnoccupiedSite; i++) {
                    final LotroPhysicalCard site = game.getGameState().getSite(i);
                    if (site.getCardController() == null)
                        return true;
                }

                return false;
            }

            @Override
            protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
                return new TakeControlOfASiteEffect(action.getActionSource(), action.getPerformingPlayer());
            }
        };
    }

}
