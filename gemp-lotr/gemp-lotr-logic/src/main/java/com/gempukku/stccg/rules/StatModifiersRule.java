package com.gempukku.stccg.rules;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.ModifiersLogic;
import com.gempukku.stccg.modifiers.StrengthModifier;
import com.gempukku.stccg.modifiers.MinionSiteNumberModifier;
import com.gempukku.stccg.modifiers.ResistanceModifier;
import com.gempukku.stccg.modifiers.VitalityModifier;

public class StatModifiersRule {
    private final ModifiersLogic modifiersLogic;

    public StatModifiersRule(ModifiersLogic modifiersLogic) {
        this.modifiersLogic = modifiersLogic;
    }

    public void applyRule() {
        modifiersLogic.addAlwaysOnModifier(
                new StrengthModifier(null, Filters.and(Filters.inPlay, Filters.character, Filters.hasAttached(Filters.any)), null,
                        new Evaluator() {
                            @Override
                            public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
                                int sum = 0;
                                for (PhysicalCard attachedCard : game.getGameState().getAttachedCards(cardAffected)) {
                                    final int strength = attachedCard.getBlueprint().getStrength();
                                    if (strength <= 0 || modifiersLogic.appliesStrengthBonusModifier(game, attachedCard, cardAffected))
                                        sum += strength;
                                }

                                return sum;
                            }
                        }, true));
        modifiersLogic.addAlwaysOnModifier(
                new VitalityModifier(null, Filters.and(Filters.inPlay, Filters.character, Filters.hasAttached(Filters.any)),
                        new Evaluator() {
                            @Override
                            public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
                                int sum = 0;
                                for (PhysicalCard attachedCard : game.getGameState().getAttachedCards(cardAffected))
                                    sum += attachedCard.getBlueprint().getVitality();

                                return sum;
                            }
                        }, true));
        modifiersLogic.addAlwaysOnModifier(
                new ResistanceModifier(null, Filters.and(Filters.inPlay, Filters.character, Filters.hasAttached(Filters.any)), null,
                        (game, cardAffected) -> {
                            int sum = 0;
                            for (PhysicalCard attachedCard : game.getGameState().getAttachedCards(cardAffected))
                                sum += attachedCard.getBlueprint().getResistance();

                            return sum;
                        }, true));
        modifiersLogic.addAlwaysOnModifier(
                new MinionSiteNumberModifier(null, Filters.and(Filters.inPlay, CardType.MINION, Filters.hasAttached(Filters.any)), null,
                        (game, cardAffected) -> {
                            int sum = 0;
                            for (PhysicalCard attachedCard : game.getGameState().getAttachedCards(cardAffected))
                                sum += attachedCard.getBlueprint().getSiteNumber();

                            return sum;
                        }, true));
    }
}
