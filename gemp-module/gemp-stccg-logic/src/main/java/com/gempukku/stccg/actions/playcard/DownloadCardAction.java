package com.gempukku.stccg.actions.playcard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.targetresolver.SelectCardsResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.List;

public class DownloadCardAction extends ActionyAction implements DownloadAction {

    @JsonProperty("playCardAction")
    @JsonIdentityReference(alwaysAsId = true)
    private Action _playCardAction;

    private final PhysicalCard _performingCard;

    private final SelectCardsResolver _cardToDownloadTarget;

    public DownloadCardAction(DefaultGame cardGame, String performingPlayerName, SelectCardsResolver cardTarget,
                              PhysicalCard performingCard) {
        super(cardGame, performingPlayerName, ActionType.DOWNLOAD_CARD);
        _cardToDownloadTarget = cardTarget;
        _performingCard = performingCard;
        _cardTargets.add(cardTarget);
    }

    protected void playCard(DefaultGame cardGame, final PhysicalCard selectedCard) throws InvalidGameLogicException {
        _playCardAction = selectedCard.getPlayCardAction(cardGame, true);
        cardGame.getActionsEnvironment().addActionToStack(_playCardAction);
    }


    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return !_cardToDownloadTarget.cannotBeResolved(cardGame);
    }

    @Override
    protected void processEffect(DefaultGame cardGame) {
        try {
            // The playCard method determines valid destinations
            playCard(cardGame, Iterables.getOnlyElement(_cardToDownloadTarget.getCards()));
            setAsSuccessful();
        } catch(InvalidGameLogicException exp) {
            cardGame.sendErrorMessage(exp);
            setAsFailed();
        }
    }

    protected Action getPlayCardAction() { return _playCardAction; }
    protected void setPlayCardAction(Action action) { _playCardAction = action; }

    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

    @Override
    public Collection<? extends PhysicalCard> getDownloadableTargets(DefaultGame cardGame) {
        return _cardToDownloadTarget.getSelectableCards(cardGame);
    }

    @Override
    public void setCardToDownload(PhysicalCard cardToDownload) {
        _cardToDownloadTarget.setSelectedCards(List.of(cardToDownload));
    }
}