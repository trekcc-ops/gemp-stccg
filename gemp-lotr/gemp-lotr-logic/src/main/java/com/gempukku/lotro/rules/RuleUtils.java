package com.gempukku.lotro.rules;

import com.gempukku.lotro.cards.LotroCardBlueprint;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.PossessionClass;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class RuleUtils {

    public static Filter getFullValidTargetFilter(String playerId, final DefaultGame game, final PhysicalCard self) {
        final LotroCardBlueprint blueprint = self.getBlueprint();
        return Filters.and(blueprint.getValidTargetFilter(playerId, game, self),
                (Filter) (game12, physicalCard) -> {
                    final CardType thisType = blueprint.getCardType();
                    if (thisType == CardType.POSSESSION || thisType == CardType.ARTIFACT) {
                        final CardType targetType = physicalCard.getBlueprint().getCardType();
                        return targetType == CardType.COMPANION || targetType == CardType.ALLY
                                || targetType == CardType.MINION;
                    }
                    return true;
                },
                (Filter) (game1, attachedTo) -> {
                    Set<PossessionClass> possessionClasses = blueprint.getPossessionClasses();
                    if (possessionClasses != null) {
                        for (PossessionClass possessionClass : possessionClasses) {
                            List<PhysicalCard> attachedCards = game1.getGameState().getAttachedCards(attachedTo);

                            Collection<PhysicalCard> matchingClassPossessions = Filters.filter(attachedCards, game1, Filters.or(CardType.POSSESSION, CardType.ARTIFACT), possessionClass);
                            if (matchingClassPossessions.size() > 1)
                                return false;

/*                            boolean extraPossessionClass = self.getBlueprint().isExtraPossessionClass(game1, self, attachedTo);
                            if (!extraPossessionClass && matchingClassPossessions.size() == 1) {
                                final LotroPhysicalCard attachedPossession = matchingClassPossessions.iterator().next();
                                if (!attachedPossession.getBlueprint().isExtraPossessionClass(game1, attachedPossession, attachedTo))
                                    return false;
                            } */
                        }
                    }
                    return true;
                });
    }

    public static boolean isAllyAtHome(PhysicalCard ally, int siteNumber, SitesBlock siteBlock) {
        final SitesBlock allySiteBlock = ally.getBlueprint().getAllyHomeSiteBlock();
        final int[] allyHomeSites = ally.getBlueprint().getAllyHomeSiteNumbers();
        if (allySiteBlock != siteBlock)
            return false;
        for (int number : allyHomeSites)
            if (number == siteNumber)
                return true;
        return false;
    }

}
