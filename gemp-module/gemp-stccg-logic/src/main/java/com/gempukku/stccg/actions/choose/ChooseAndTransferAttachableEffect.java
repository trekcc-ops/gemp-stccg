package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.TransferPermanentEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.decisions.CardsSelectionDecision;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.filters.Filter;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;
import java.util.Set;

public class ChooseAndTransferAttachableEffect extends DefaultEffect {
    private final Action _action;
    private final String _playerId;
    private final Filterable _attachedTo;
    private final Filterable _attachedCard;
    private final Filterable _transferTo;
    private final boolean _skipOriginalTargetCheck;
    private final DefaultGame _game;

    public ChooseAndTransferAttachableEffect(DefaultGame game, Action action, String playerId, Filterable attachedCard, Filterable attachedTo, Filterable transferTo) {
        this(game, action, playerId, false, attachedCard, attachedTo, transferTo);
    }

    public ChooseAndTransferAttachableEffect(DefaultGame game, Action action, String playerId, boolean skipOriginalTargetCheck, Filterable attachedCard, Filterable attachedTo, Filterable transferTo) {
        super(playerId);
        _action = action;
        _playerId = playerId;
        _skipOriginalTargetCheck = skipOriginalTargetCheck;
        _attachedCard = attachedCard;
        _attachedTo = attachedTo;
        _transferTo = transferTo;
        _game = game;
    }

    private Filterable getValidTargetFilter(DefaultGame game, final PhysicalCard attachment) {
        if (_skipOriginalTargetCheck) {
            return Filters.and(
                    _transferTo,
                    Filters.not(attachment.getAttachedTo()),
                    (Filter) (game12, target) -> game12.getModifiersQuerying().canHaveTransferredOn(game12, attachment, target));
        } else {
            return Filters.and(
                    _transferTo,
                    attachment.getFullValidTargetFilter(),
                    Filters.not(attachment.getAttachedTo()),
                    (Filter) (game1, target) -> game1.getModifiersQuerying().canHaveTransferredOn(game1, attachment, target));
        }
    }

    private Collection<PhysicalCard> getPossibleAttachmentsToTransfer() {
        return Filters.filterActive(_game,
                _attachedCard,
                Filters.attachedTo(_attachedTo),
                (Filter) (game1, transferredCard) -> {
                    if (transferredCard.getBlueprint().getValidTargetFilter() == null)
                        return false;

                    if (!_game.getModifiersQuerying().canBeTransferred(transferredCard))
                        return false;

                    return Filters.countActive(_game, getValidTargetFilter(game1, transferredCard))>0;
                });
    }

    @Override
    public boolean isPlayableInFull() {
        return !getPossibleAttachmentsToTransfer().isEmpty();
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        final Collection<PhysicalCard> possibleAttachmentsToTransfer = getPossibleAttachmentsToTransfer();
        if (!possibleAttachmentsToTransfer.isEmpty()) {
            _game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new CardsSelectionDecision(1, "Choose card to transfer", possibleAttachmentsToTransfer, 1, 1) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            final Set<PhysicalCard> selectedAttachments = getSelectedCardsByResponse(result);
                            if (selectedAttachments.size() == 1) {
                                final PhysicalCard attachment = selectedAttachments.iterator().next();
                                final PhysicalCard transferredFrom = attachment.getAttachedTo();
                                final Collection<PhysicalCard> validTargets = Filters.filterActive(_game, getValidTargetFilter(_game, attachment));
                                _game.getUserFeedback().sendAwaitingDecision(
                                        _playerId,
                                        new CardsSelectionDecision(1, "Choose transfer target", validTargets, 1, 1) {
                                            @Override
                                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                                final Set<PhysicalCard> selectedTargets = getSelectedCardsByResponse(result);
                                                if (selectedTargets.size() == 1) {
                                                    final PhysicalCard selectedTarget = selectedTargets.iterator().next();
                                                    SubAction subAction = _action.createSubAction();
                                                    subAction.appendEffect(
                                                            new TransferPermanentEffect(_game, attachment, selectedTarget) {
                                                                @Override
                                                                protected void afterTransferredCallback() {
                                                                    afterTransferCallback(attachment, transferredFrom, selectedTarget);
                                                                }
                                                            });
                                                    _game.getActionsEnvironment().addActionToStack(subAction);
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

    protected void afterTransferCallback(PhysicalCard transferredCard, PhysicalCard transferredFrom, PhysicalCard transferredTo) {

    }
}
