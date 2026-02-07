package com.gempukku.stccg.actions.draw;

import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

public class DrawCardsAction extends ActionyAction implements TopLevelSelectableAction {

    private final PhysicalCard _performingCard;
    private int _cardsAlreadyDrawnCount;
    private final int _totalCardsToDraw;

    public DrawCardsAction(DefaultGame cardGame, PhysicalCard performingCard, String performingPlayerName, int count) {
        super(cardGame, performingPlayerName, ActionType.DRAW_CARD);
        _performingCard = performingCard;
        _totalCardsToDraw = count;
    }

    public DrawCardsAction(DefaultGame cardGame, PhysicalCard performingCard, String performingPlayerName) {
        this(cardGame, performingCard, performingPlayerName, 1);
    }

    public DrawCardsAction(PhysicalCard performingCard, String performingPlayerName, int count, DefaultGame cardGame) {
        this(cardGame, performingCard, performingPlayerName, count);
    }

    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        try {
            Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
            return performingPlayer.getCardsInDrawDeck().size() >= _totalCardsToDraw;
        } catch(PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            return false;
        }
    }

    @Override
    protected void processEffect(DefaultGame cardGame) {
        if (_cardsAlreadyDrawnCount < _totalCardsToDraw) {
            _cardsAlreadyDrawnCount++;
            cardGame.addActionToStack(new DrawSingleCardAction(cardGame, _performingPlayerId));
        } else {
            setAsSuccessful();
        }
    }

}