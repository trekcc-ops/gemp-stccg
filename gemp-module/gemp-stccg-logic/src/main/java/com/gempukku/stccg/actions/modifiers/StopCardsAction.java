package com.gempukku.stccg.actions.modifiers;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.actions.targetresolver.ActionCardResolver;
import com.gempukku.stccg.actions.targetresolver.FixedCardsResolver;
import com.gempukku.stccg.actions.targetresolver.SelectCardsResolver;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

import java.util.Collection;

public class StopCardsAction extends ActionyAction {
    private final ActionCardResolver _cardTarget;
    private final String _saveToMemoryId;
    private Collection<PhysicalCard> _stoppedCards;

    public StopCardsAction(DefaultGame cardGame, String performingPlayerName,
                           ActionCardResolver cardTarget, GameTextContext context, String saveToMemoryId) {
        super(cardGame, performingPlayerName, ActionType.STOP_CARDS, context);
        _cardTarget = cardTarget;
        _cardTargets.add(cardTarget);
        _saveToMemoryId = saveToMemoryId;
    }

    private StopCardsAction(DefaultGame cardGame, String performingPlayerName, ActionCardResolver cardResolver) {
        this(cardGame, performingPlayerName, cardResolver, null, null);
    }

    public StopCardsAction(DefaultGame cardGame, String performingPlayerName,
                           Collection<? extends ST1EPhysicalCard> cardsToStop) {
        this(cardGame, performingPlayerName, new FixedCardsResolver(cardsToStop));
    }

    public StopCardsAction(DefaultGame cardGame, String performingPlayerName, SelectCardsAction selectionAction) {
        this(cardGame, performingPlayerName, new SelectCardsResolver(selectionAction));
    }



    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    protected void processEffect(DefaultGame cardGame) {
        try {
            _stoppedCards = _cardTarget.getCards(cardGame);
            for (PhysicalCard card : _cardTarget.getCards(cardGame)) {
                if (card instanceof ST1EPhysicalCard stCard) {
                    stCard.stop();
                } else {
                    setAsFailed();
                    throw new InvalidGameLogicException("Tried to \"stop\" a card from a game with no \"stop\" action");
                }
            }
            if (_actionContext != null && _saveToMemoryId != null) {
                _actionContext.setCardMemory(_saveToMemoryId, _stoppedCards);
            }
            setAsSuccessful();
        } catch(InvalidGameLogicException exp) {
            cardGame.sendErrorMessage(exp);
            setAsFailed();
        }
    }

    @JsonProperty("targetCardIds")
    @JsonIdentityReference(alwaysAsId=true)
    public Collection<PhysicalCard> getStoppedCards() {
        return _stoppedCards;
    }

}