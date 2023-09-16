package com.gempukku.lotro.effects.choose;

import com.gempukku.lotro.cards.lotronly.LotroPhysicalCard;
import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.decisions.CardsSelectionDecision;
import com.gempukku.lotro.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.effects.TransferPermanentEffect;
import com.gempukku.lotro.actions.lotronly.SubAction;
import com.gempukku.lotro.effects.AbstractEffect;
import com.gempukku.lotro.actions.Action;
import com.gempukku.lotro.rules.RuleUtils;

import java.util.Collection;
import java.util.Set;

public class ChooseAndTransferAttachableEffect extends AbstractEffect {
    private final Action _action;
    private final String _playerId;
    private final Filterable _attachedTo;
    private final Filterable _attachedCard;
    private final Filterable _transferTo;
    private final boolean _skipOriginalTargetCheck;

    public ChooseAndTransferAttachableEffect(Action action, String playerId, Filterable attachedCard, Filterable attachedTo, Filterable transferTo) {
        this(action, playerId, false, attachedCard, attachedTo, transferTo);
    }

    public ChooseAndTransferAttachableEffect(Action action, String playerId, boolean skipOriginalTargetCheck, Filterable attachedCard, Filterable attachedTo, Filterable transferTo) {
        _action = action;
        _playerId = playerId;
        _skipOriginalTargetCheck = skipOriginalTargetCheck;
        _attachedCard = attachedCard;
        _attachedTo = attachedTo;
        _transferTo = transferTo;
    }

    private Filterable getValidTargetFilter(DefaultGame game, final LotroPhysicalCard attachment) {
        if (_skipOriginalTargetCheck) {
            return Filters.and(
                    _transferTo,
                    Filters.not(attachment.getAttachedTo()),
                    (Filter) (game12, target) -> game12.getModifiersQuerying().canHaveTransferredOn(game12, attachment, target));
        } else {
            return Filters.and(
                    _transferTo,
                    RuleUtils.getFullValidTargetFilter(attachment.getOwner(), game, attachment),
                    Filters.not(attachment.getAttachedTo()),
                    (Filter) (game1, target) -> game1.getModifiersQuerying().canHaveTransferredOn(game1, attachment, target));
        }
    }

    private Collection<LotroPhysicalCard> getPossibleAttachmentsToTransfer(final DefaultGame game) {
        return Filters.filterActive(game,
                _attachedCard,
                Filters.attachedTo(_attachedTo),
                (Filter) (game1, transferredCard) -> {
                    if (transferredCard.getBlueprint().getValidTargetFilter(transferredCard.getOwner(), game1, transferredCard) == null)
                        return false;

                    if (!game1.getModifiersQuerying().canBeTransferred(game1, transferredCard))
                        return false;

                    return Filters.countActive(game1, getValidTargetFilter(game1, transferredCard))>0;
                });
    }

    @Override
    public boolean isPlayableInFull(DefaultGame game) {
        return getPossibleAttachmentsToTransfer(game).size() > 0;
    }

    @Override
    protected FullEffectResult playEffectReturningResult(final DefaultGame game) {
        final Collection<LotroPhysicalCard> possibleAttachmentsToTransfer = getPossibleAttachmentsToTransfer(game);
        if (possibleAttachmentsToTransfer.size() > 0) {
            game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new CardsSelectionDecision(1, "Choose card to transfer", possibleAttachmentsToTransfer, 1, 1) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            final Set<LotroPhysicalCard> selectedAttachments = getSelectedCardsByResponse(result);
                            if (selectedAttachments.size() == 1) {
                                final LotroPhysicalCard attachment = selectedAttachments.iterator().next();
                                final LotroPhysicalCard transferredFrom = attachment.getAttachedTo();
                                final Collection<LotroPhysicalCard> validTargets = Filters.filterActive(game, getValidTargetFilter(game, attachment));
                                game.getUserFeedback().sendAwaitingDecision(
                                        _playerId,
                                        new CardsSelectionDecision(1, "Choose transfer target", validTargets, 1, 1) {
                                            @Override
                                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                                final Set<LotroPhysicalCard> selectedTargets = getSelectedCardsByResponse(result);
                                                if (selectedTargets.size() == 1) {
                                                    final LotroPhysicalCard selectedTarget = selectedTargets.iterator().next();
                                                    SubAction subAction = new SubAction(_action);
                                                    subAction.appendEffect(
                                                            new TransferPermanentEffect(attachment, selectedTarget) {
                                                                @Override
                                                                protected void afterTransferredCallback() {
                                                                    afterTransferCallback(attachment, transferredFrom, selectedTarget);
                                                                }
                                                            });
                                                    game.getActionsEnvironment().addActionToStack(subAction);
                                                }
                                            }
                                        }
                                );
                            }
                        }
                    });
        }
        return new FullEffectResult(false);
    }

    protected void afterTransferCallback(LotroPhysicalCard transferredCard, LotroPhysicalCard transferredFrom, LotroPhysicalCard transferredTo) {

    }
}
