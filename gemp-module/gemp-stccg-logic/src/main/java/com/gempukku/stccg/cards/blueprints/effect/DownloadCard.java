package com.gempukku.stccg.cards.blueprints.effect;

import com.fasterxml.jackson.databind.JsonNode;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.StackActionEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.ConstantValueSource;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.cards.PlayerSource;
import com.gempukku.stccg.cards.blueprints.CardBlueprintFactory;
import com.gempukku.stccg.cards.blueprints.FilterableSource;
import com.gempukku.stccg.cards.blueprints.ValueSource;
import com.gempukku.stccg.cards.blueprints.resolver.CardResolver;
import com.gempukku.stccg.cards.blueprints.resolver.ValueResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.modifiers.ModifierFlag;

import java.util.Collection;

public class DownloadCard implements EffectAppenderProducer {
        // TODO - Adapted from PlayCardFromDrawDeck EffectAppenderProducer. Eventually would like to append this as a DownloadAction.
    @Override
    public EffectBlueprint createEffectAppender(JsonNode effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        // TODO - Assumes the base definition of "download" for 1E, i.e. can search through eligible decks,
        // find card, and immediately play. Assumes no target for downloading is given (e.g., "download to your outpost")
        environment.validateAllowedFields(effectObject, "filter", "nocheck");

        final String filter = effectObject.get("filter").textValue();
        final boolean noCheck = environment.getBoolean(effectObject, "nocheck", false);
        final String memorize = "_temp";

        ValueSource countSource = new ConstantValueSource(1);
        if(noCheck)
        {
            // This range will cause choice checks to succeed even if no valid choices are found (which is how draw deck
            // searching is supposed to work RAW).  However, we don't want this to be the default, else dual-choice
            // cards that play "from draw deck or discard pile" would allow empty sources to be chosen, which is NPE.
            countSource = ValueResolver.resolveEvaluator("0-1");
        }

        MultiEffectBlueprint result = new MultiEffectBlueprint();
        result.setPlayabilityCheckedForEffect(true);

        FilterableSource playableCardsFilter = (actionContext) -> Filters.playable;
        PlayerSource you = ActionContext::getPerformingPlayerId;

        EffectBlueprint targetCardAppender = CardResolver.resolveCardsInZone(filter, playableCardsFilter, countSource,
                memorize, you, you, "Choose card to play",
                environment.getCardFilterableIfChooseOrAll(filter), Zone.DRAW_DECK, false,
                environment.getCardSourceFromZone(you, Zone.DRAW_DECK, filter));


        result.addEffectAppender(targetCardAppender);
        result.addEffectAppender(
                new DefaultDelayedAppender() {
                    @Override
                    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
                        final Collection<? extends PhysicalCard> cardsToPlay =
                                context.getCardsFromMemory(memorize);
                        if (cardsToPlay.size() == 1) {
                            return new StackActionEffect(context.getGame(),
                                    cardsToPlay.iterator().next().getPlayCardAction(true));
                                        // TODO - Because personnel/ships/equipment use ReportCardAction, this only works to download other card types
                        } else {
                            return null;
                        }
                    }

                    @Override
                    public boolean isPlayableInFull(ActionContext actionContext) {
                        return !actionContext.getGame().getModifiersQuerying()
                                .hasFlagActive(ModifierFlag.CANT_PLAY_FROM_DISCARD_OR_DECK);
                    }

                    @Override
                    public boolean isPlayabilityCheckedForEffect() { return true; }
                });

        return result;
    }
}