package com.gempukku.stccg.actions.playcard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.targetresolver.EnterPlayAtDestinationResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ProxyCoreCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;
import java.util.List;

public class DownloadCardAction extends ActionyAction implements DownloadAction {

    private final PhysicalCard _performingCard;

    private final EnterPlayAtDestinationResolver _targetResolver;

    public DownloadCardAction(DefaultGame cardGame, String performingPlayerName,
                              EnterPlayAtDestinationResolver cardTarget, PhysicalCard performingCard) {
        super(cardGame, performingPlayerName, ActionType.DOWNLOAD_CARD);
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
        if (cardEnteringPlay == null || destinationCard == null) {
            cardGame.sendErrorMessage("Unable to resolve download card action");
            setAsFailed();
        } else {
            cardGame.removeCardsFromZone(List.of(cardEnteringPlay));
            if (destinationCard instanceof ProxyCoreCard) {
                cardGame.getGameState()
                        .addCardToZone(cardGame, cardEnteringPlay, destinationCard.getZone(), _actionContext);
            } else {
                cardEnteringPlay.setAsAtop(destinationCard);
            }
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