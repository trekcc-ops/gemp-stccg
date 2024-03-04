package com.gempukku.stccg.actions;

import com.gempukku.stccg.actions.choose.ChooseActiveCardsEffect;
import com.gempukku.stccg.actions.lotr.PayTwilightCostEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.modifiers.ModifierFlag;

import java.util.Collection;

public class TransferPermanentAction extends ActivateCardAction {

    public TransferPermanentAction(final PhysicalCard card, Filter filter) {
        super(card);
        setText("Transfer " + card.getFullName());

        appendCost(
                new UnrespondableEffect() {
                    @Override
                    protected void doPlayEffect() {
                        if (!_game.getModifiersQuerying().hasFlagActive(ModifierFlag.TRANSFERS_FOR_FREE))
                            insertCost(new PayTwilightCostEffect(_game, card));
                    }
                });
        appendEffect(
                new ChooseActiveCardsEffect(card, card.getOwnerName(), "Choose target to attach to",
                        1, 1, filter) {
                    @Override
                    protected void cardsSelected(Collection<PhysicalCard> target) {
                        if (!target.isEmpty()) {
                            appendEffect(new TransferPermanentEffect(_game, card, target.iterator().next()));
                        }
                    }
                });
    }

    @Override
    public ActionType getActionType() {
        return ActionType.TRANSFER;
    }
}
