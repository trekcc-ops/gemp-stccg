package com.gempukku.stccg.rules;

import com.gempukku.stccg.actions.AttachPermanentAction;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.PlayEventAction;
import com.gempukku.stccg.actions.PlayPermanentAction;
import com.gempukku.stccg.cards.CardBlueprint;
import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

public class PlayUtils {
    protected static Zone getPlayToZone(PhysicalCard card) {
        final CardType cardType = card.getBlueprint().getCardType();
        return switch (cardType) {
            case COMPANION -> Zone.FREE_CHARACTERS;
            case MINION -> Zone.SHADOW_CHARACTERS;
            default -> Zone.SUPPORT;
        };
    }

    public static final Map<Phase, Keyword> PhaseKeywordMap = ImmutableMap.copyOf(new HashMap<>() {{
        put(Phase.FELLOWSHIP, Keyword.FELLOWSHIP);
        put(Phase.SHADOW, Keyword.SHADOW);
        put(Phase.MANEUVER, Keyword.MANEUVER);
        put(Phase.ARCHERY, Keyword.ARCHERY);
        put(Phase.ASSIGNMENT, Keyword.ASSIGNMENT);
        put(Phase.SKIRMISH, Keyword.SKIRMISH);
        put(Phase.REGROUP, Keyword.REGROUP);
    }});

    protected static Filter getFullAttachValidTargetFilter(final DefaultGame game, final PhysicalCard card,
                                                         int twilightModifier) {
        return Filters.and(RuleUtils.getFullValidTargetFilter(card.getOwner(), game, card),
                (Filter) (game1, physicalCard) -> game1.getModifiersQuerying().canHavePlayedOn(game1, card, physicalCard),
                (Filter) (game12, physicalCard) -> {
                    if (card.getBlueprint().getSide() == Side.SHADOW) {
                        final int twilightCostOnTarget = game12.getModifiersQuerying().getTwilightCost(game12, card,
                                physicalCard, twilightModifier, false);
                        int potentialDiscount = game12.getModifiersQuerying().getPotentialDiscount(game12, card);
                        return twilightCostOnTarget - potentialDiscount <= game12.getGameState().getTwilightPool();
                    } else {
                        return true;
                    }
                });
    }


    public static CostToEffectAction getPlayCardAction(DefaultGame game, PhysicalCard card, int twilightModifier, Filterable additionalAttachmentFilter, boolean ignoreRoamingPenalty) {
        final CardBlueprint blueprint = card.getBlueprint();

        if (blueprint.getCardType() != CardType.EVENT) {
            final Filterable validTargetFilter = blueprint.getValidTargetFilter(card.getOwner(), game, card);
            if (validTargetFilter == null) {
                PlayPermanentAction action = new PlayPermanentAction(card, getPlayToZone(card), twilightModifier, ignoreRoamingPenalty);

                game.getModifiersQuerying().appendExtraCosts(game, action, card);
                game.getModifiersQuerying().appendPotentialDiscounts(game, action, card);

                return action;
            } else {
                final AttachPermanentAction action = new AttachPermanentAction(game, card, Filters.and(getFullAttachValidTargetFilter(game, card, twilightModifier), additionalAttachmentFilter), twilightModifier);

                game.getModifiersQuerying().appendPotentialDiscounts(game, action, card);
                game.getModifiersQuerying().appendExtraCosts(game, action, card);

                return action;
            }
        } else {
            final PlayEventAction action = blueprint.getPlayEventCardAction(card.getOwner(), game, card);

            game.getModifiersQuerying().appendPotentialDiscounts(game, action, card);
            game.getModifiersQuerying().appendExtraCosts(game, action, card);

            return action;
        }
    }
}
