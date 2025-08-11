package com.gempukku.stccg.rules.st1e;

import com.gempukku.stccg.cards.CardWithHullIntegrity;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.GameLocation;
import com.gempukku.stccg.player.Player;
import org.apache.logging.log4j.core.net.Facility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShipBattleRules {

    public static Map<PhysicalCard, Map<String, List<PhysicalCard>>> getTargetsForShipBattleInitiation(
            ST1EGame stGame, Player initiatingPlayer) {

        List<CardWithHullIntegrity> initiatingPlayerCards = new ArrayList<>();
        List<CardWithHullIntegrity> defendingCards = new ArrayList<>();

        for (PhysicalCard card : stGame.getGameState().getAllCardsInPlay()) {
            if (card instanceof CardWithHullIntegrity hullCard) {
                if (card.isControlledBy(initiatingPlayer) && hullCard.getWeapons(stGame) > 0 && hullCard.hasLeaderPresent()) {
                    initiatingPlayerCards.add(hullCard);
                } else if (!card.isControlledBy(initiatingPlayer)) {
                    defendingCards.add(hullCard);
                }
            }
        }

        Map<PhysicalCard, Map<String, List<PhysicalCard>>> targetMap = new HashMap<>();

        for (CardWithHullIntegrity attackingCard1 : initiatingPlayerCards) {
            List<PhysicalCard> canTargetCards = new ArrayList<>();
            for (CardWithHullIntegrity defendingCard : defendingCards) {
                if (attackingCard1.getGameLocation() == defendingCard.getGameLocation()) {
                    if (cardCanAttackOtherCardPerAffiliationAttackRestrictions(attackingCard1, defendingCard)) {
                        canTargetCards.add(defendingCard);
                    }
                }
            }
            if (!canTargetCards.isEmpty()) {
                List<PhysicalCard> canAttackWithCards = new ArrayList<>();
                for (CardWithHullIntegrity attackingCard2 : initiatingPlayerCards) {
                        // TODO - This is pretty ugly but it gets the job done
                    if (attackingCard1 != attackingCard2 && attackingCard2 instanceof PhysicalNounCard1E nounCard2 &&
                            attackingCard1.isCompatibleWith(nounCard2) &&
                            attackingCard1.getGameLocation() == attackingCard2.getGameLocation()) {
                        canAttackWithCards.add(attackingCard2);
                    }
                }
                Map<String, List<PhysicalCard>> thisCardMap = new HashMap<>();
                thisCardMap.put("canAttackWith", canAttackWithCards);
                thisCardMap.put("canTarget", canTargetCards);
                targetMap.put(attackingCard1, thisCardMap);
            }
        }

        return targetMap;
    }

    public static Map<PhysicalCard, Map<String, List<PhysicalCard>>> getTargetsForReturningFire(
            DefaultGame cardGame, Player defendingPlayer, CardWithHullIntegrity targetCard,
            List<CardWithHullIntegrity> attackingForce) {

        GameLocation location = targetCard.getGameLocation();
        List<CardWithHullIntegrity> firingCards = new ArrayList<>();

        for (PhysicalCard card : cardGame.getGameState().getAllCardsInPlay()) {
            if (card instanceof CardWithHullIntegrity hullCard && card.isAtLocation(location)) {
                if (card.isControlledBy(defendingPlayer) && hullCard.getWeapons(cardGame) > 0) {
                    firingCards.add(hullCard);
                }
            }
        }

        Map<PhysicalCard, Map<String, List<PhysicalCard>>> targetMap = new HashMap<>();

        for (CardWithHullIntegrity attackingCard1 : firingCards) {
            List<PhysicalCard> canTargetCards = new ArrayList<>(attackingForce);
            if (!canTargetCards.isEmpty()) {
                List<PhysicalCard> canAttackWithCards = new ArrayList<>();
                for (CardWithHullIntegrity attackingCard2 : firingCards) {
                    // TODO - This is pretty ugly but it gets the job done
                    if (attackingCard1 != attackingCard2 && attackingCard2 instanceof PhysicalNounCard1E nounCard2 &&
                            attackingCard1.isCompatibleWith(nounCard2) &&
                            attackingCard1.getGameLocation() == attackingCard2.getGameLocation()) {
                        canAttackWithCards.add(attackingCard2);
                    }
                }
                Map<String, List<PhysicalCard>> thisCardMap = new HashMap<>();
                thisCardMap.put("canAttackWith", canAttackWithCards);
                thisCardMap.put("canTarget", canTargetCards);
                targetMap.put(attackingCard1, thisCardMap);
            }
        }

        return targetMap;
    }

    private static boolean cardCanAttackOtherCardPerAffiliationAttackRestrictions(CardWithHullIntegrity attackingCard,
                                                                           CardWithHullIntegrity defendingCard) {
        if (attackingCard instanceof AffiliatedCard attacker && defendingCard instanceof AffiliatedCard defender) {
            if (attacker.hasAffiliationInList(List.of(Affiliation.KLINGON, Affiliation.KAZON, Affiliation.NON_ALIGNED, Affiliation.NEUTRAL))) {
                return true;
            } else if (attacker.isAffiliation(Affiliation.FEDERATION)) {
                return defender.isAffiliation(Affiliation.BORG);
            } else {
                return !attacker.matchesAffiliationOf(defender);
            }
        } else {
            // otherwise assume one is self-controlled
            // TODO have not sorted this logic yet for two self-controlled cards attacking each other
            return true;
        }
    }

}