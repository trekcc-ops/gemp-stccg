package com.gempukku.stccg.actions.modifiers;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.actions.discard.DiscardSingleCardAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalReportableCard1E;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.google.common.collect.Iterables;

public class KillSinglePersonnelAction extends ActionyAction implements TopLevelSelectableAction {

    private final PhysicalCard _performingCard;
    private final ActionCardResolver _cardTarget;
    private PhysicalCard _victim;

    public KillSinglePersonnelAction(String performingPlayerName, PhysicalCard performingCard,
                                     SelectCardsAction selectVictimAction) {
        this(performingPlayerName, performingCard, new SelectCardsResolver(selectVictimAction));
    }

    public KillSinglePersonnelAction(DefaultGame cardGame, String performingPlayerName, PhysicalCard performingCard,
                                     ActionCardResolver targetResolver) {
        super(cardGame, performingPlayerName, "Kill", ActionType.KILL);
        _performingCard = performingCard;
        _cardTarget = targetResolver;
    }

    public KillSinglePersonnelAction(String performingPlayerName, PhysicalCard performingCard,
                                     ActionCardResolver targetResolver) {
        super(performingCard.getGame(), performingPlayerName, "Kill", ActionType.KILL);
        _performingCard = performingCard;
        _cardTarget = targetResolver;
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
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        if (!_cardTarget.isResolved()) {
            Action selectionAction = _cardTarget.getSelectionAction();
            if (selectionAction != null && !selectionAction.wasCarriedOut()) {
                return selectionAction;
            } else {
                _cardTarget.resolve(cardGame);
            }
        }

        if (!_wasCarriedOut) {
            if (_cardTarget.getCards(cardGame).size() != 1) {
                throw new InvalidGameLogicException("Too many cards selected for KillSinglePersonnelAction");
            } else {
                _victim = Iterables.getOnlyElement(_cardTarget.getCards(cardGame));

                if (_victim instanceof PhysicalReportableCard1E reportable && reportable.getAwayTeam() != null)
                    reportable.leaveAwayTeam((ST1EGame) cardGame);
                _wasCarriedOut = true;
                return new DiscardSingleCardAction(_performingCard, cardGame.getPlayer(_performingPlayerId), _victim);
            }
        }
        setAsSuccessful();
        saveResult(new KillCardResult(this, _victim));
        return getNextAction();
    }

    @SuppressWarnings("unused")
    @JsonProperty("targetCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private PhysicalCard victimCard() {
        return _victim;
    }
}