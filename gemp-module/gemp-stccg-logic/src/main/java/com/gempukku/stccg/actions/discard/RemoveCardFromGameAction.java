package com.gempukku.stccg.actions.discard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.targetresolver.ActionCardResolver;
import com.gempukku.stccg.actions.targetresolver.FixedCardResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.GameLocation;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.gamestate.ST1EGameState;

import java.util.*;

public class RemoveCardFromGameAction extends ActionyAction {

    private final ActionCardResolver _cardTarget;

    public RemoveCardFromGameAction(DefaultGame cardGame, String performingPlayerName, ActionCardResolver resolver) {
        super(cardGame, performingPlayerName, ActionType.REMOVE_CARD_FROM_GAME);
        _cardTarget = resolver;
        _cardTargets.add(_cardTarget);
    }

    public RemoveCardFromGameAction(DefaultGame cardGame, String performingPlayerName, PhysicalCard cardToRemove) {
        super(cardGame, performingPlayerName, ActionType.REMOVE_CARD_FROM_GAME);
        _cardTarget = new FixedCardResolver(cardToRemove);
        _cardTargets.add(_cardTarget);
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @JsonProperty("targetCardIds")
    @JsonIdentityReference(alwaysAsId=true)
    private Collection<PhysicalCard> targetCardIds() {
        return _cardTarget.getCards();
    }

    public void processEffect(DefaultGame cardGame) {
        Collection<PhysicalCard> removedCards = new ArrayList<>(_cardTarget.getCards());
        if (cardGame instanceof ST1EGame stGame) {
            ST1EGameState gameState = stGame.getGameState();

            for (PhysicalCard card : removedCards) {
                for (GameLocation location : gameState.getOrderedSpacelineLocations()) {
                    if (location instanceof MissionLocation mission && mission.getSeedCards().contains(card)) {
                        mission.removeSeedCard(card);
                    }
                }
            }
        }
        cardGame.getGameState().removeCardsFromZoneWithoutSendingToClient(cardGame, new ArrayList<>(removedCards));
        for (PhysicalCard removedCard : removedCards) {
            cardGame.getGameState().addCardToRemovedPile(removedCard);
            if (removedCard instanceof ST1EPhysicalCard stCard && stCard.isStopped()) {
                stCard.unstop();
            }
        }
        saveResult(new RemoveCardFromGameActionResult(cardGame, this, removedCards), cardGame);
        setAsSuccessful();
    }
}