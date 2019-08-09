package com.gempukku.lotro.cards.set40.isengard;

import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.modifiers.condition.LocationCondition;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.modifiers.KeywordModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;

import java.util.Collections;
import java.util.List;

/**
 * Title: Uruk Bloodthirster
 * Set: Second Edition
 * Side: Shadow
 * Culture: Isengard
 * Twilight Cost: 4
 * Type: Minion - Uruk-hai
 * Strength: 9
 * Vitality: 2
 * Home: 5
 * Card Number: 1U145
 * Game Text: Damage +1. While at a battleground, this minion is damage +1.
 */
public class Card40_145 extends AbstractMinion {
    public Card40_145() {
        super(4, 9, 2, 5, Race.URUK_HAI, Culture.ISENGARD, "Uruk Bloodthirster");
        addKeyword(Keyword.DAMAGE, 1);
    }

    @Override
    public List<? extends Modifier> getAlwaysOnModifiers(LotroGame game, PhysicalCard self) {
        KeywordModifier modifier = new KeywordModifier(self, self,
                new LocationCondition(Keyword.BATTLEGROUND), Keyword.DAMAGE, 1);
        return Collections.singletonList(modifier);
    }
}