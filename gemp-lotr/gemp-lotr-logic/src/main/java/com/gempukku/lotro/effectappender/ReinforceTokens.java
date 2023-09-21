package com.gempukku.lotro.effectappender;

import com.gempukku.lotro.cards.CardGenerationEnvironment;
import com.gempukku.lotro.actioncontext.DefaultActionContext;
import com.gempukku.lotro.cards.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.ValueSource;
import com.gempukku.lotro.fieldprocessor.FieldUtils;
import com.gempukku.lotro.effectappender.resolver.CardResolver;
import com.gempukku.lotro.effectappender.resolver.ValueResolver;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Token;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.cards.LotroPhysicalCard;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.actions.CostToEffectAction;
import com.gempukku.lotro.effects.AddTokenEffect;
import com.gempukku.lotro.modifiers.ModifierFlag;
import com.gempukku.lotro.evaluator.ConstantEvaluator;
import com.gempukku.lotro.effects.Effect;
import org.json.simple.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class ReinforceTokens implements EffectAppenderProducer {
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        FieldUtils.validateAllowedFields(effectObject, "amount", "culture", "filter", "memorize");

        final ValueSource valueSource = ValueResolver.resolveEvaluator(effectObject.get("amount"), 1, environment);
        final Culture culture = FieldUtils.getEnum(Culture.class, effectObject.get("culture"), "culture");
        final Token token = (culture != null) ? Token.findTokenForCulture(culture) : null;
        final String filter = FieldUtils.getString(effectObject.get("filter"), "filter", "self");
        final String memory = FieldUtils.getString(effectObject.get("memorize"), "memorize", "_temp");

        MultiEffectAppender result = new MultiEffectAppender();

        result.addEffectAppender(
                CardResolver.resolveCards(filter, (actionContext) -> Filters.hasToken(token, 1),
                        new ConstantEvaluator(1), memory, "you", "Choose card to reinforce tokens on", environment));
        result.addEffectAppender(
                new DelayedAppender<>() {
                    @Override
                    public boolean isPlayableInFull(DefaultActionContext<DefaultGame> actionContext) {
                        final DefaultGame game = actionContext.getGame();
                        return !game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.CANT_TOUCH_CULTURE_TOKENS)
                            && Filters.countActive(game, Filters.hasToken(token)) > 0;
                    }

                    @Override
                    protected List<Effect> createEffects(boolean cost, CostToEffectAction action, DefaultActionContext actionContext) {
                        final Collection<? extends LotroPhysicalCard> cardsFromMemory = actionContext.getCardsFromMemory(memory);

                        final int tokenCount = valueSource.getEvaluator(actionContext).evaluateExpression(actionContext.getGame(), null);

                        List<Effect> result = new LinkedList<>();
                        for (LotroPhysicalCard card : cardsFromMemory)
                            result.add(new AddTokenEffect(actionContext.getSource(), card, token, tokenCount));

                        return result;
                    }
                });

        return result;
    }

}
