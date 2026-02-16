package com.gempukku.stccg.actions.discard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.playcard.PlayCardResult;
import com.gempukku.stccg.actions.targetresolver.ActionCardResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.game.DefaultGame;
import com.google.common.collect.Iterables;

import java.util.Collection;

public class NullifyCardAction extends ActionyAction implements DiscardAction, TopLevelSelectableAction {

    @JsonProperty("performingCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final PhysicalCard _performingCard;

    @JsonProperty("targetCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private PhysicalCard _cardToNullify;

    private final ActionCardResolver _nullifiedCardResolver;

    public NullifyCardAction(DefaultGame cardGame, PhysicalCard performingCard, String performingPlayerName,
                             ActionCardResolver nullifiedCardResolver) {
        super(cardGame, performingPlayerName, ActionType.NULLIFY);
        _nullifiedCardResolver = nullifiedCardResolver;
        _cardTargets.add(_nullifiedCardResolver);
        _performingCard = performingCard;
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
    public void processEffect(DefaultGame cardGame) {
        Collection<PhysicalCard> cardsToNullify = _nullifiedCardResolver.getCards(cardGame);
        if (cardsToNullify.size() == 1) {
            _cardToNullify = Iterables.getOnlyElement(cardsToNullify);
            for (Action action : cardGame.getActionsEnvironment().getActionStack()) {
                if (action.getResult() instanceof PlayCardResult playResult &&
                        playResult.getPlayedCard() == _cardToNullify) {
                    action.setAsFailed();
                }
            }
            if (_cardToNullify.getCardType() == CardType.DILEMMA) {
                cardGame.addActionToStack(new RemoveDilemmaFromGameAction(cardGame, _performingPlayerId, _cardToNullify));
            } else {
                cardGame.addActionToStack(new DiscardSingleCardAction(cardGame, _performingCard, _performingPlayerId,
                        _cardToNullify));
            }
            setAsSuccessful();
        } else {
            cardGame.sendErrorMessage("Tried to nullify too many cards at once");
            setAsFailed();
        }
    }

    @JsonIgnore
    public PhysicalCard getNullifiedCard() {
        return _cardToNullify;
    }

}