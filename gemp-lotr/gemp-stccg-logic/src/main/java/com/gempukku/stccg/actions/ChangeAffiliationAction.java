package com.gempukku.stccg.actions;

import com.gempukku.stccg.actions.choose.ChooseAffiliationEffect;
import com.gempukku.stccg.cards.CardWithCrew;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalReportableCard1E;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ChangeAffiliationAction extends AbstractCostToEffectAction {
    private final PhysicalReportableCard1E _card;
    private boolean _affiliationWasChosen = false;
    private Affiliation _selectedAffiliation;
    private boolean _actionCompleted = false;
    private final List<Affiliation> _affiliationOptions = new LinkedList<>();
    private final ST1EGame _game;

    public ChangeAffiliationAction(Player player, PhysicalReportableCard1E card) {
                    // TODO - Zone is null because these will be attached and the implementation is weird
        super(player, ActionType.OTHER);
        _card = card;
        _game = card.getGame();
        _card.getAffiliationOptions().forEach(affiliation -> {
            if (affiliation != _card.getCurrentAffiliation())
                _affiliationOptions.add(affiliation);
        });
        if (_card.getAttachedTo() != null && _card.getAttachedTo() instanceof CardWithCrew cardWithCrew) {
            _affiliationOptions.removeIf(affiliation ->
                    !_card.isCompatibleWithFacilityOrShipAndItsCrewAsAffiliation(cardWithCrew, affiliation));
        }
    }

    @Override
    public boolean canBeInitiated() {
        return !_affiliationOptions.isEmpty();
    }


    @Override
    public String getText() { return "Change affiliation"; }

    @Override
    public PhysicalCard getActionSource() { return _card; }

    @Override
    public PhysicalCard getActionAttachedToCard() { return _card; }

    @Override
    public Effect nextEffect() {

        if (!_affiliationWasChosen) {
            if (_affiliationOptions.size() > 1) {
                appendCost(new ChooseAffiliationEffect(_game, getPerformingPlayerId(), new ArrayList<>(_affiliationOptions)) {
                    @Override
                    protected void affiliationChosen(Affiliation affiliation) {
                        _selectedAffiliation = affiliation;
                    }
                });
                return getNextCost();
            } else _selectedAffiliation = _affiliationOptions.get(0);
            _affiliationWasChosen = true;
        }

        if (!_actionCompleted) {
            _game.getGameState().sendMessage(_performingPlayerId + " changed " +
                    _card.getCardLink() + "'s affiliation to " + _selectedAffiliation.toHTML());
            _card.setCurrentAffiliation(_selectedAffiliation);
            _actionCompleted = true;
        }

        return getNextEffect();
    }

    @Override
    public ST1EGame getGame() { return _game; }
}