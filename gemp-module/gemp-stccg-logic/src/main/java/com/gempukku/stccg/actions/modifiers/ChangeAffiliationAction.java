package com.gempukku.stccg.actions.modifiers;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.choose.SelectAffiliationAction;
import com.gempukku.stccg.cards.CardWithCrew;
import com.gempukku.stccg.cards.physicalcard.AffiliatedCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;

import java.util.LinkedList;
import java.util.List;

public class ChangeAffiliationAction extends ActionyAction implements TopLevelSelectableAction {
    private final AffiliatedCard _performingCard;
    private final SelectAffiliationAction _selectAffiliationAction;

    public ChangeAffiliationAction(Player player, AffiliatedCard card) {
        super(player, "Change affiliation", ActionType.OTHER);
        _performingCard = card;
        List<Affiliation> _affiliationOptions = new LinkedList<>();
        _performingCard.getAffiliationOptions().forEach(affiliation -> {
            if (affiliation != _performingCard.getAffiliation())
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
        _selectAffiliationAction = new SelectAffiliationAction(player, _affiliationOptions);
    }

    public boolean requirementsAreMet(DefaultGame cardGame) {
        return !_selectAffiliationAction.requirementsAreMet(cardGame);
    }


    @Override
    public PhysicalCard getPerformingCard() { return _performingCard; }

    @Override
    public int getCardIdForActionSelection() { return _performingCard.getCardId(); }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {

        if (!_selectAffiliationAction.wasCarriedOut())
            return _selectAffiliationAction;

        if (!_wasCarriedOut) {
            Affiliation selectedAffiliation = _selectAffiliationAction.getSelectedAffiliation();
            cardGame.sendMessage(_performingPlayerId + " changed " +
                    _performingCard.getCardLink() + "'s affiliation to " + selectedAffiliation.toHTML());
            _performingCard.changeAffiliation(selectedAffiliation);
            _wasCarriedOut = true;
        }

        return getNextAction();
    }

}