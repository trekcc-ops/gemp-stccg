package com.gempukku.stccg.actions.placecard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.targetresolver.ActionCardResolver;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.targetresolver.SelectCardsResolver;
import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

import java.util.List;

public class PlaceCardOnBottomOfPlayPileAction extends ActionyAction {

    @JsonIdentityReference(alwaysAsId=true)
    @JsonProperty("cardTarget")
    private final ActionCardResolver _cardTarget;

    public PlaceCardOnBottomOfPlayPileAction(DefaultGame cardGame, Player performingPlayer,
                                             SelectCardsAction selectCardAction) {
        super(cardGame, performingPlayer, ActionType.PLACE_CARD_IN_PLAY_PILE);
        _cardTarget = new SelectCardsResolver(selectCardAction);
        _cardTargets.add(_cardTarget);
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @Override
    protected void processEffect(DefaultGame cardGame) {
        for (PhysicalCard card : _cardTarget.getCards()) {
            cardGame.getGameState().removeCardsFromZoneWithoutSendingToClient(cardGame, List.of(card));
            cardGame.getGameState().addCardToZone(cardGame, card, Zone.PLAY_PILE, _actionContext);
        }
    }
}