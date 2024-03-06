package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.effectappender.resolver.CardResolver;
import com.gempukku.stccg.effectappender.resolver.PlayerResolver;
import com.gempukku.stccg.effectappender.resolver.ValueResolver;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.StackActionEffect;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.modifiers.ModifierFlag;
import org.json.simple.JSONObject;

import java.util.Collection;

public class DownloadCard implements EffectAppenderProducer {
        // TODO - Adapted from PlayCardFromDrawDeck EffectAppenderProducer. Eventually would like to append this as a DownloadAction.
    @Override
    public EffectAppender createEffectAppender(JSONObject effectObject, CardBlueprintFactory environment)
            throws InvalidCardDefinitionException {
        // TODO - Assumes the base definition of "download" for 1E, i.e. can search through eligible decks,
        // find card, and immediately play. Assumes no target for downloading is given (e.g., "download to your outpost")
        environment.validateAllowedFields(effectObject, "filter", "nocheck");

        final String filter = environment.getString(effectObject.get("filter"), "filter");
        final boolean noCheck = environment.getBoolean(effectObject.get("nocheck"), "nocheck", false);
        final String memorize = "_temp";

        ValueSource countSource = new ConstantValueSource(1);
        if(noCheck)
        {
            // This range will cause choice checks to succeed even if no valid choices are found (which is how draw deck
            // searching is supposed to work RAW).  However, we don't want this to be the default, else dual-choice
            // cards that play "from draw deck or discard pile" would allow empty sources to be chosen, which is NPE.
            countSource = ValueResolver.resolveEvaluator("0-1", 1, environment);
        }

        MultiEffectAppender result = new MultiEffectAppender();
        result.setPlayabilityCheckedForEffect(true);

        PlayerSource you = PlayerResolver.resolvePlayer("you");

        result.addEffectAppender(
                CardResolver.resolveCardsInZone(filter, (actionContext) -> Filters.playable(actionContext.getGame()),
                        countSource, memorize, you, you,
                        "Choose card to download", environment, Zone.DRAW_DECK));
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
