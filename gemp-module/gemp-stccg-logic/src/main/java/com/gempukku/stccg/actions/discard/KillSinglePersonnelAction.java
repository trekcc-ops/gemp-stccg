package com.gempukku.stccg.actions.discard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.actions.choose.SelectCardsAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.google.common.collect.Iterables;

public class KillSinglePersonnelAction extends ActionyAction implements DiscardAction, TopLevelSelectableAction {

    private final PhysicalCard _performingCard;
    private final ActionCardResolver _cardTarget;
    private PhysicalCard _victim;

    public KillSinglePersonnelAction(DefaultGame cardGame, String performingPlayerName, PhysicalCard performingCard,
                                     ActionCardResolver targetResolver) {
        super(cardGame, performingPlayerName, "Kill", ActionType.KILL);
        _performingCard = performingCard;
        _cardTarget = targetResolver;
        _cardTargets.add(targetResolver);
    }

    public KillSinglePersonnelAction(DefaultGame cardGame, String performingPlayerName, PhysicalCard performingCard,
                                     SelectCardsAction selectVictimAction) {
        this(cardGame, performingPlayerName, performingCard, new SelectCardsResolver(selectVictimAction));
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
        try {
            if (_cardTarget.getCards().size() != 1) {
                setAsFailed();
                throw new InvalidGameLogicException("Too many cards selected for KillSinglePersonnelAction");
            } else {
                _victim = Iterables.getOnlyElement(_cardTarget.getCards(cardGame));
                discardCard(_victim, cardGame);
                saveResult(new KillCardResult(this, _victim), cardGame);
                setAsSuccessful();
            }
        } catch(InvalidGameLogicException exp) {
            cardGame.sendErrorMessage(exp);
            setAsFailed();
        }
    }

    @SuppressWarnings("unused")
    @JsonProperty("targetCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private PhysicalCard victimCard() {
        return _victim;
    }
}