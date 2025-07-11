package com.gempukku.stccg.rules.st1e;

import com.gempukku.stccg.cards.CardWithHullIntegrity;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.player.Player;
import org.apache.logging.log4j.core.net.Facility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShipBattleRules {

    public static Map<PhysicalCard, Map<String, List<PhysicalCard>>> getTargetsForShipBattleInitiation(
            ST1EGame stGame, Player initiatingPlayer) {

        // Structure of initiation matrix:
            // Attacking force - each ship + compatible ships
            // Defending target - each ship + ships that can attack it

        List<CardWithHullIntegrity> initiatingPlayerCards = new ArrayList<>();
        List<CardWithHullIntegrity> defendingCards = new ArrayList<>();

        /*
            For each initiating card candidate, include:
                cards they are compatible with
                cards they can attack
         */

        // Expect back from client - action id, attacking force, target

        for (PhysicalCard card : stGame.getGameState().getAllCardsInPlay()) {
            if (card instanceof CardWithHullIntegrity hullCard) {
                if (card.isControlledBy(initiatingPlayer) && hullCard.getWeapons(stGame) > 0) {
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
                    // TODO - check affiliation attack restrictions
                    canTargetCards.add(defendingCard);
                }
            }
            if (!defendingCards.isEmpty()) {
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

}