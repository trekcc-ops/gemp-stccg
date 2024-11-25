package com.gempukku.stccg.actions;

import com.gempukku.stccg.actions.choose.ChooseAffiliationEffect;
import com.gempukku.stccg.cards.CardWithCrew;
import com.gempukku.stccg.cards.physicalcard.AffiliatedCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ChangeAffiliationAction extends ActionyAction {
    private final AffiliatedCard _card;
    private boolean _affiliationWasChosen;
    private Affiliation _selectedAffiliation;
    private boolean _actionCompleted;
    private final List<Affiliation> _affiliationOptions = new LinkedList<>();

    public ChangeAffiliationAction(Player player, AffiliatedCard card) {
        super(player, "Change affiliation", ActionType.OTHER);
        _card = card;
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
                    personnel -> !personnel.isCompatibleWith(affiliation)));
        } else {
            // There should be no other types of cards that would get the ChangeAffiliationAction
            _affiliationOptions.clear();
        }
    }

    public boolean requirementsAreMet(DefaultGame cardGame) {
        return !_affiliationOptions.isEmpty();
    }


    @Override
    public PhysicalCard getActionSource() { return (PhysicalCard) _card; }

    @Override
    public PhysicalCard getCardForActionSelection() { return (PhysicalCard) _card; }

    @Override
    public Action nextAction(DefaultGame cardGame) {
        Player player = cardGame.getPlayer(_performingPlayerId);

        if (!_affiliationWasChosen) {
            if (_affiliationOptions.size() > 1) {
                appendCost(new ChooseAffiliationEffect(player, new ArrayList<>(_affiliationOptions)) {
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
            cardGame.sendMessage(_performingPlayerId + " changed " +
                    _card.getCardLink() + "'s affiliation to " + _selectedAffiliation.toHTML());
            _card.changeAffiliation(_selectedAffiliation);
            _actionCompleted = true;
        }

        return getNextAction();
    }

}