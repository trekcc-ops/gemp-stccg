package com.gempukku.stccg.actions.discard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.CardPerformedAction;
import com.gempukku.stccg.actions.targetresolver.ActionCardResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KillAction extends ActionyAction implements DiscardAction, CardPerformedAction {

    private final PhysicalCard _performingCard;
    private final ActionCardResolver _cardTarget;
    private List<PhysicalCard> _cardsToKill;
    private List<PhysicalCard> _cardsKilled = new ArrayList<>();
    private boolean _killResultSent;

    public KillAction(DefaultGame cardGame, String performingPlayerName, PhysicalCard performingCard,
                      ActionCardResolver targetResolver) {
        super(cardGame, performingPlayerName, ActionType.KILL);
        _performingCard = performingCard;
        _cardTarget = targetResolver;
        _cardTargets.add(targetResolver);
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
        if (_cardsToKill == null) {
            _cardsToKill = new ArrayList<>(_cardTarget.getCards().stream().toList());
        } else if (!_cardsToKill.isEmpty()) {
            PhysicalCard nextVictim = _cardsToKill.getFirst();
            if (!_killResultSent) {
                saveResult(new KillCardResult(cardGame, this, nextVictim), cardGame);
                _killResultSent = true;
            } else {
                _cardsToKill.remove(nextVictim);
                _cardsKilled.add(nextVictim);
                discardCard(nextVictim, cardGame);
                saveResult(new DiscardCardResult(cardGame, nextVictim, this, Zone.DISCARD), cardGame);
                _killResultSent = false;
            }
        } else if (_cardsKilled.isEmpty()) {
            setAsFailed();
        } else {
            setAsSuccessful();
        }
    }

    @JsonProperty("killedCardIds")
    @JsonIdentityReference(alwaysAsId=true)
    public Collection<PhysicalCard> getKilledCards() {
        return _cardsKilled;
    }

    @Override
    public Zone getDestination() {
        return Zone.DISCARD;
    }
}