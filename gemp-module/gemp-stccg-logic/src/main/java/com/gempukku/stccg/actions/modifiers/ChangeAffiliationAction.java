package com.gempukku.stccg.actions.modifiers;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.choose.SelectAffiliationAction;
import com.gempukku.stccg.cards.CardWithCrew;
import com.gempukku.stccg.cards.physicalcard.AffiliatedCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.game.*;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.LinkedList;
import java.util.List;

public class ChangeAffiliationAction extends ActionyAction implements TopLevelSelectableAction {
    @JsonProperty("targetCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final AffiliatedCard _performingCard;
    private SelectAffiliationAction _selectAffiliationAction;

    public ChangeAffiliationAction(ST1EGame cardGame, Player player, AffiliatedCard card) {
        super(cardGame, player, "Change affiliation", ActionType.CHANGE_AFFILIATION);
        _performingCard = card;
    }

    public boolean requirementsAreMet(DefaultGame cardGame) {
        return !getAffiliationOptions(cardGame).isEmpty();
    }


    @Override
    public PhysicalCard getPerformingCard() { return _performingCard; }

    private List<Affiliation> getAffiliationOptions(DefaultGame cardGame) {
        List<Affiliation> _affiliationOptions = new LinkedList<>();
        _performingCard.getAffiliationOptions().forEach(affiliation -> {
            if (affiliation != _performingCard.getCurrentAffiliation())
                _affiliationOptions.add(affiliation);
        });
        if (_performingCard instanceof PersonnelCard personnel) {
            if (personnel.getAttachedTo(cardGame) != null && personnel.getAttachedTo(cardGame) instanceof CardWithCrew cardWithCrew) {
                _affiliationOptions.removeIf(affiliation ->
                        !personnel.isCompatibleWithCardAndItsCrewAsAffiliation(cardWithCrew, affiliation));
            }
        } else if (_performingCard instanceof CardWithCrew cardWithCrew) {
            // TODO - Ignores carried ship interactions
            _affiliationOptions.removeIf(affiliation -> cardWithCrew.getPersonnelInCrew().stream().anyMatch(
                    personnel -> !personnel.isCompatibleWith(affiliation)));
        } else {
            // There should be no other types of cards that would get the ChangeAffiliationAction
            _affiliationOptions.clear();
        }
        return _affiliationOptions;
    }


    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        if (_selectAffiliationAction == null) {
            _selectAffiliationAction = new SelectAffiliationAction(
                    cardGame, cardGame.getPlayer(_performingPlayerId), getAffiliationOptions(cardGame));
        }


        if (!_selectAffiliationAction.wasCarriedOut())
            return _selectAffiliationAction;

        if (!_wasCarriedOut) {
            Affiliation selectedAffiliation = _selectAffiliationAction.getSelectedAffiliation();
            _performingCard.changeAffiliation(selectedAffiliation);
            _wasCarriedOut = true;
            setAsSuccessful();
        }

        return getNextAction();
    }

}