package com.gempukku.stccg.actions.discard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.google.common.collect.Iterables;

import java.util.List;

public class DiscardSingleCardAction extends ActionyAction implements TopLevelSelectableAction {
    @JsonProperty("performingCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final PhysicalCard _performingCard;
    private final ActionCardResolver _cardTarget;
    @JsonProperty("targetCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private PhysicalCard _discardedCard;

    public DiscardSingleCardAction(DefaultGame cardGame, PhysicalCard performingCard, String performingPlayerName,
                                   ActionCardResolver cardResolver) {
        super(cardGame, performingPlayerName, "Discard", ActionType.DISCARD);
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
    protected void continueInitiation(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        super.continueInitiation(cardGame);
        if (_cardTarget.isResolved()) {
            if (_cardTarget.getCards(cardGame).size() == 1) {
                _discardedCard = Iterables.getOnlyElement(_cardTarget.getCards(cardGame));
            } else {
                throw new InvalidGameLogicException("Got too many cards for DiscardSingleCardAction");
            }
        }
    }


    @Override
    protected void processEffect(DefaultGame cardGame) {
        GameState gameState = cardGame.getGameState();
        gameState.removeCardsFromZoneWithoutSendingToClient(cardGame, List.of(_discardedCard));
        if (_discardedCard instanceof ST1EPhysicalCard stCard && stCard.isStopped()) {
            stCard.unstop();
        }
        cardGame.addCardToTopOfDiscardPile(_discardedCard);
        saveResult(new DiscardCardFromPlayResult(_discardedCard, this));
        setAsSuccessful();
    }

}