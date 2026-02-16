package com.gempukku.stccg.actions.discard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.targetresolver.ActionCardResolver;
import com.gempukku.stccg.actions.targetresolver.FixedCardResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.google.common.collect.Iterables;

import java.util.Collection;

public class DiscardSingleCardAction extends ActionyAction implements DiscardAction, TopLevelSelectableAction {
    @JsonProperty("performingCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final PhysicalCard _performingCard;
    private final ActionCardResolver _cardTarget;
    @JsonProperty("targetCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private PhysicalCard _discardedCard;

    public DiscardSingleCardAction(DefaultGame cardGame, PhysicalCard performingCard, String performingPlayerName,
                                   ActionCardResolver cardResolver) {
        super(cardGame, performingPlayerName, ActionType.DISCARD);
        _performingCard = performingCard;
        _cardTarget = cardResolver;
        _cardTargets.add(_cardTarget);
    }


    public DiscardSingleCardAction(DefaultGame cardGame, PhysicalCard performingCard, String performingPlayerName,
                                   PhysicalCard cardToDiscard) {
        this(cardGame, performingCard, performingPlayerName, new FixedCardResolver(cardToDiscard));
    }


    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    protected void processEffect(DefaultGame cardGame) {
        Collection<? extends PhysicalCard> cardTargets = _cardTarget.getCards(cardGame);
        if (cardTargets.size() == 1) {
            _discardedCard = Iterables.getOnlyElement(cardTargets);
            discardCard(_discardedCard, cardGame);
            saveResult(new DiscardCardFromPlayResult(_discardedCard, this), cardGame);
            setAsSuccessful();
        } else {
            cardGame.sendErrorMessage("Too many cards received for discard action");
            setAsFailed();
        }
    }

    @Override
    public Zone getDestination() {
        return Zone.DISCARD;
    }
}