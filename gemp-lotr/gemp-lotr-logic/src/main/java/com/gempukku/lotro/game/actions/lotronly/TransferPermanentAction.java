package com.gempukku.lotro.game.actions.lotronly;

import com.gempukku.lotro.cards.lotronly.LotroPhysicalCard;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.game.actions.ActivateCardAction;
import com.gempukku.lotro.game.rules.GameUtils;
import com.gempukku.lotro.game.effects.ChooseActiveCardsEffect;
import com.gempukku.lotro.game.effects.PayTwilightCostEffect;
import com.gempukku.lotro.game.effects.TransferPermanentEffect;
import com.gempukku.lotro.game.modifiers.ModifierFlag;
import com.gempukku.lotro.game.effects.UnrespondableEffect;

import java.util.Collection;

public class TransferPermanentAction extends ActivateCardAction {

    public TransferPermanentAction(final LotroPhysicalCard card, Filter filter) {
        super(card);
        setText("Transfer " + GameUtils.getFullName(card));

        appendCost(
                new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect(DefaultGame game) {
                        if (!game.getModifiersQuerying().hasFlagActive(game, ModifierFlag.TRANSFERS_FOR_FREE))
                            insertCost(new PayTwilightCostEffect(card));
                    }
                });
        appendEffect(
                new ChooseActiveCardsEffect(null, card.getOwner(), "Choose target to attach to", 1, 1, filter) {
                    @Override
                    protected void cardsSelected(DefaultGame game, Collection<LotroPhysicalCard> target) {
                        if (target.size() > 0) {
                            appendEffect(new TransferPermanentEffect(card, target.iterator().next()));
                        }
                    }
                });
    }

    @Override
    public Type getType() {
        return Type.TRANSFER;
    }
}
