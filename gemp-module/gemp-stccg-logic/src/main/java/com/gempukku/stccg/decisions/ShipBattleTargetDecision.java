package com.gempukku.stccg.decisions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.CardWithHullIntegrity;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

import java.util.*;

public class ShipBattleTargetDecision extends AbstractAwaitingDecision {

    @JsonProperty("independentlySelectable")
    private final boolean _independentlySelectable = true;

    private List<CardWithHullIntegrity> _attackingCards;
    private CardWithHullIntegrity _defendingTarget;

    private final Map<PhysicalCard, Map<String, List<PhysicalCard>>> _targetMap;

    public ShipBattleTargetDecision(String playerName, DecisionContext context,
                                    Map<PhysicalCard, Map<String, List<PhysicalCard>>> targetMap,
                                    DefaultGame cardGame) {
        super(playerName, context, cardGame);
        _targetMap = targetMap;
    }

    public ShipBattleTargetDecision(Player player, DecisionContext context,
                                    Map<PhysicalCard, Map<String, List<PhysicalCard>>> targetMap,
                                    DefaultGame cardGame) {
        super(player, context, cardGame);
        _targetMap = targetMap;
    }

    public String getElementType() { return "CARD"; }

    @Override
    public void decisionMade(String result) throws DecisionResultInvalidException {
        // don't do anything, wip
    }

    private boolean attackingCardsAreCompatible(PhysicalCard card1, PhysicalCard card2) {
        if (_targetMap.get(card1) == null || _targetMap.get(card2) == null) {
            return false;
        } else {
            List<PhysicalCard> compatibleCards1 = _targetMap.get(card1).get("canAttackWith");
            List<PhysicalCard> compatibleCards2 = _targetMap.get(card2).get("canAttackWith");
            if (compatibleCards1 == null || !compatibleCards1.contains(card2))
                return false;
            return compatibleCards2 != null && compatibleCards2.contains(card1);
        }
    }

    private boolean targetCardIsCompatible(PhysicalCard attackingCard, PhysicalCard targetCard) {
        if (_targetMap.get(attackingCard) == null) {
            return false;
        } else {
            List<PhysicalCard> validTargets = _targetMap.get(attackingCard).get("canTarget");
            return validTargets != null && validTargets.contains(targetCard);
        }
    }

    public void decisionMade(List<CardWithHullIntegrity> attackingCards, CardWithHullIntegrity defendingTarget)
            throws DecisionResultInvalidException {
        boolean passesValidation = true;
        for (CardWithHullIntegrity attackingCard1 : attackingCards) {
            for (CardWithHullIntegrity attackingCard2 : attackingCards) {
                if (attackingCard1 != attackingCard2 && !attackingCardsAreCompatible(attackingCard1, attackingCard2))
                    passesValidation = false;
            }
            if (!targetCardIsCompatible(attackingCard1, defendingTarget))
                passesValidation = false;
        }

        if (passesValidation) {
            _attackingCards = new ArrayList<>();
            _attackingCards.addAll(attackingCards);
            _defendingTarget = defendingTarget;
        } else {
            throw new DecisionResultInvalidException("Cards received by decision did not pass validation");
        }
    }

    public List<CardWithHullIntegrity> getAttackingCards() {
        return Collections.unmodifiableList(_attackingCards);
    }

    public CardWithHullIntegrity getTarget() {
        return _defendingTarget;
    }

}