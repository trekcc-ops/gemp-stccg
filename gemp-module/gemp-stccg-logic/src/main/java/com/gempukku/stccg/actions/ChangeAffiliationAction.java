package com.gempukku.stccg.actions;

import com.gempukku.stccg.actions.choose.ChooseAffiliationEffect;
import com.gempukku.stccg.cards.CardWithCrew;
import com.gempukku.stccg.cards.physicalcard.AffiliatedCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ChangeAffiliationAction extends AbstractCostToEffectAction {
    private final AffiliatedCard _card;
    private boolean _affiliationWasChosen = false;
    private Affiliation _selectedAffiliation;
    private boolean _actionCompleted = false;
    private final List<Affiliation> _affiliationOptions = new LinkedList<>();
    private final ST1EGame _game;

    public ChangeAffiliationAction(Player player, AffiliatedCard card) {
        super(player, ActionType.OTHER);
        _card = card;
        _game = card.getGame();
        _card.getAffiliationOptions().forEach(affiliation -> {
            if (affiliation != _card.getAffiliation())
                _affiliationOptions.add(affiliation);
        });
        if (card instanceof PersonnelCard personnel) {
            if (personnel.getAttachedTo() != null && personnel.getAttachedTo() instanceof CardWithCrew cardWithCrew) {
                _affiliationOptions.removeIf(affiliation ->
                        !personnel.isCompatibleWithCardAndItsCrewAsAffiliation(cardWithCrew, affiliation));
            }
        } else if (card instanceof CardWithCrew cardWithCrew) {
                // TODO - Ignores carried ship interactions
            _affiliationOptions.removeIf(affiliation -> cardWithCrew.getPersonnelInCrew().stream().anyMatch(
                    personnel -> personnel.isNotCompatibleWith(affiliation)));
        } else {
            // There should be no other types of cards that would get the ChangeAffiliationAction
            _affiliationOptions.clear();
        }
    }

    @Override
    public boolean canBeInitiated() {
        return !_affiliationOptions.isEmpty();
    }


    @Override
    public String getText() { return "Change affiliation"; }

    @Override
    public PhysicalCard getActionSource() { return (PhysicalCard) _card; }

    @Override
    public PhysicalCard getActionAttachedToCard() { return (PhysicalCard) _card; }

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
            } else _selectedAffiliation = _affiliationOptions.getFirst();
            _affiliationWasChosen = true;
        }

        if (!_actionCompleted) {
            _game.sendMessage(_performingPlayerId + " changed " +
                    _card.getCardLink() + "'s affiliation to " + _selectedAffiliation.toHTML());
            _card.setCurrentAffiliation(_selectedAffiliation);
            _actionCompleted = true;
        }

        return getNextEffect();
    }

    @Override
    public ST1EGame getGame() { return _game; }
}