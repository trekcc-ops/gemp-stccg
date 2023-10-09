package com.gempukku.stccg.actions;

import com.gempukku.stccg.cards.PhysicalCard;
import com.gempukku.stccg.effects.choose.ChooseActiveCardsEffect;
import com.gempukku.stccg.effects.PayTwilightCostEffect;
import com.gempukku.stccg.effects.TransferPermanentEffect;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.rules.GameUtils;
import com.gempukku.stccg.modifiers.ModifierFlag;
import com.gempukku.stccg.effects.UnrespondableEffect;

import java.util.Collection;

public class TransferPermanentAction extends ActivateCardAction {

    public TransferPermanentAction(final PhysicalCard card, Filter filter) {
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
                    protected void cardsSelected(DefaultGame game, Collection<PhysicalCard> target) {
                        if (target.size() > 0) {
                            appendEffect(new TransferPermanentEffect(card, target.iterator().next()));
                        }
                    }
                });
    }

    @Override
    public ActionType getActionType() {
        return ActionType.TRANSFER;
    }
}
