package com.gempukku.stccg.actions.discard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.choose.SelectVisibleCardAction;
import com.gempukku.stccg.actions.targetresolver.ActionCardResolver;
import com.gempukku.stccg.actions.targetresolver.AllCardsMatchingFilterResolver;
import com.gempukku.stccg.actions.targetresolver.FixedCardsResolver;
import com.gempukku.stccg.actions.targetresolver.SelectCardsResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.filters.CardFilter;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.player.Player;

import java.util.Collection;

public class TribblesMultiDiscardActionBroken extends ActionyAction implements TopLevelSelectableAction {

    /* TODO - There is a bug in this class, because each individual discard action is not created as a
        separate action. */

    private final PhysicalCard _performingCard;
    private final ActionCardResolver _cardTarget;
    private Collection<PhysicalCard> _cardsDiscarded; // may not be initialized

    public TribblesMultiDiscardActionBroken(DefaultGame cardGame, PhysicalCard performingCard, String performingPlayerName,
                                            ActionCardResolver cardTarget) {
        super(cardGame, performingPlayerName, ActionType.DISCARD);
        _performingCard = performingCard;
        _cardTarget = cardTarget;
        _cardTargets.add(cardTarget);
    }

    public TribblesMultiDiscardActionBroken(DefaultGame cardGame, PhysicalCard performingCard,
                                            String performingPlayerName, SelectVisibleCardAction selectAction) {
        this(cardGame, performingCard, performingPlayerName, new SelectCardsResolver(selectAction));
    }

    public TribblesMultiDiscardActionBroken(DefaultGame cardGame, PhysicalCard performingCard, Player performingPlayer,
                                            SelectVisibleCardAction selectAction) {
        this(cardGame, performingCard, performingPlayer.getPlayerId(), new SelectCardsResolver(selectAction));
    }

    public TribblesMultiDiscardActionBroken(DefaultGame cardGame, PhysicalCard performingCard,
                                            String performingPlayerName, Collection<PhysicalCard> cardsToDiscard) {
        this(cardGame, performingCard, performingPlayerName, new FixedCardsResolver(cardsToDiscard));
    }

    public TribblesMultiDiscardActionBroken(DefaultGame cardGame, PhysicalCard performingCard,
                                            String performingPlayerName, CardFilter cardFilter) {
        this(cardGame, performingCard, performingPlayerName, new AllCardsMatchingFilterResolver(cardFilter));
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
        Collection<PhysicalCard> cardsToDiscard = _cardTarget.getCards(cardGame);
        GameState gameState = cardGame.getGameState();
        gameState.removeCardsFromZoneWithoutSendingToClient(cardGame, cardsToDiscard);
        for (PhysicalCard cardToDiscard : cardsToDiscard) {
            cardGame.addCardToTopOfDiscardPile(cardToDiscard);
            saveResult(new DiscardCardFromPlayResult(cardToDiscard, this), cardGame);
        }
        setAsSuccessful();
    }

    @JsonProperty("targetCardIds")
    @JsonIdentityReference(alwaysAsId=true)
    private Collection<PhysicalCard> cardsDiscarded() {
        return _cardsDiscarded;
    }

}