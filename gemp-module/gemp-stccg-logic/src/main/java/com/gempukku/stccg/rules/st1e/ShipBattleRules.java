package com.gempukku.stccg.rules.st1e;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalNounCard1E;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.player.Player;

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

        List<PhysicalNounCard1E> initiatingPlayerCards = new ArrayList<>();
        List<PhysicalNounCard1E> defendingCards = new ArrayList<>();

        /*
            For each initiating card candidate, include:
                cards they are compatible with
                cards they can attack
         */

        // Expect back from client - action id, attacking force, target

            // TODO - Only works for ships, not facilities
            // TODO - does not check for WEAPONS
        for (PhysicalCard card : stGame.getGameState().getAllCardsInPlay()) {
            if (card instanceof PhysicalShipCard shipCard) {
                if (card.isControlledBy(initiatingPlayer)) {
                    initiatingPlayerCards.add(shipCard);
                } else {
                    defendingCards.add(shipCard);
                }
            }
        }

        Map<PhysicalCard, Map<String, List<PhysicalCard>>> targetMap = new HashMap<>();

        for (PhysicalNounCard1E attackingCard1 : initiatingPlayerCards) {
            List<PhysicalCard> canTargetCards = new ArrayList<>();
            for (PhysicalNounCard1E defendingCard : defendingCards) {
                if (attackingCard1.getGameLocation() == defendingCard.getGameLocation()) {
                    // TODO - check affiliation attack restrictions
                    canTargetCards.add(defendingCard);
                }
            }
            if (!defendingCards.isEmpty()) {
                List<PhysicalCard> canAttackWithCards = new ArrayList<>();
                for (PhysicalNounCard1E attackingCard2 : initiatingPlayerCards) {
                    if (attackingCard1 != attackingCard2 && attackingCard1.isCompatibleWith(attackingCard2) &&
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