package com.gempukku.stccg.rules;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.evaluator.Evaluator;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.modifiers.ModifiersLogic;
import com.gempukku.stccg.modifiers.StrengthModifier;
import com.gempukku.stccg.modifiers.VitalityModifier;

public class StatModifiersRule {
    private final ModifiersLogic modifiersLogic;

    public StatModifiersRule(ModifiersLogic modifiersLogic) {
        this.modifiersLogic = modifiersLogic;
    }

    public void applyRule() {
        modifiersLogic.addAlwaysOnModifier(
                new StrengthModifier(modifiersLogic,
                        Filters.and(Filters.inPlay, Filters.character, Filters.hasAttached(Filters.any)), null,
                        new Evaluator(modifiersLogic) {
                            @Override
                            public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
                                int sum = 0;
                                for (PhysicalCard attachedCard : cardAffected.getAttachedCards()) {
                                    final int strength = attachedCard.getBlueprint().getAttribute(CardAttribute.STRENGTH);
                                    if (strength <= 0 || modifiersLogic.appliesStrengthBonusModifier(attachedCard, cardAffected))
                                        sum += strength;
                                }

                                return sum;
                            }
                        }, true));
        modifiersLogic.addAlwaysOnModifier(
                new VitalityModifier(null, Filters.and(Filters.inPlay, Filters.character, Filters.hasAttached(Filters.any)),
                        new Evaluator(modifiersLogic) {
                            @Override
                            public int evaluateExpression(DefaultGame game, PhysicalCard cardAffected) {
                                int sum = 0;
                                for (PhysicalCard attachedCard : cardAffected.getAttachedCards())
                                    sum += attachedCard.getBlueprint().getVitality();

                                return sum;
                            }
                        }, true));
    }
}
