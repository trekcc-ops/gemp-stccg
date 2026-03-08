package com.gempukku.stccg.actions.playcard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.targetresolver.EnterPlayAtDestinationResolver;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ProxyCoreCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.Collection;
import java.util.List;

public class DownloadCardAction extends ActionWithSubActions implements DownloadAction {

    private final PhysicalCard _performingCard;

    private final EnterPlayAtDestinationResolver _targetResolver;

    public DownloadCardAction(DefaultGame cardGame, String performingPlayerName,
                              EnterPlayAtDestinationResolver cardTarget, PhysicalCard performingCard,
                              GameTextContext context) {
        super(cardGame, performingPlayerName, ActionType.DOWNLOAD_CARD, context);
        _targetResolver = cardTarget;
        _performingCard = performingCard;
        _cardTargets.add(cardTarget);
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return !_targetResolver.cannotBeResolved(cardGame);
    }

    @Override
    protected void processEffect(DefaultGame cardGame) {
        PhysicalCard cardEnteringPlay = _targetResolver.getCardEnteringPlay();
        PhysicalCard destinationCard = _targetResolver.getDestination();
        Player performingPlayer = null;
        try {
            performingPlayer = cardGame.getPlayer(_performingPlayerId);
        } catch(PlayerNotFoundException ignored) {

        }
        if (cardEnteringPlay == null || destinationCard == null || performingPlayer == null) {
            cardGame.sendErrorMessage("Unable to resolve download card action");
            setAsFailed();
        } else {
            boolean isFromDrawDeck = cardEnteringPlay.isInDrawDeck(cardGame);
            cardGame.removeCardsFromZone(List.of(cardEnteringPlay));
            if (isFromDrawDeck) {
                cardGame.shuffleCardPile(performingPlayer.getDrawDeck());
            }
            if (destinationCard instanceof ProxyCoreCard) {
                cardGame.getGameState()
                        .addCardToZone(cardGame, cardEnteringPlay, destinationCard.getZone(), _actionContext);
            } else {
                cardEnteringPlay.setAsAtop(destinationCard);
            }
            saveResult(new PlayCardResult(cardGame, this, cardEnteringPlay, destinationCard, ActionType.DOWNLOAD_CARD), cardGame);
            setAsSuccessful();
        }
    }

    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

    @Override
    public Collection<? extends PhysicalCard> getDownloadableTargets(DefaultGame cardGame) {
        return _targetResolver.getSelectableOptions();
    }

    @Override
    public void selectCardToDownload(PhysicalCard cardToDownload) throws DecisionResultInvalidException {
        if (_targetResolver.getSelectableOptions().contains(cardToDownload)) {
            _targetResolver.setCardEnteringPlay(cardToDownload);
        } else {
            throw new DecisionResultInvalidException("Cannot download selected card");
        }
    }

    @JsonProperty("playCardAction")
    @JsonIdentityReference(alwaysAsId = true)
    private Action getPlayCardAction() {
        return this;
    }

}