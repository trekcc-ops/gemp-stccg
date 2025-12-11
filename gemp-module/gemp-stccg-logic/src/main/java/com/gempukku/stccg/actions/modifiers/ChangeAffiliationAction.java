package com.gempukku.stccg.actions.modifiers;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.choose.SelectAffiliationAction;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.player.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class ChangeAffiliationAction extends ActionyAction implements TopLevelSelectableAction {
    @JsonProperty("targetCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final AffiliatedCard _performingCard;
    private final SelectAffiliationAction _selectAffiliationAction;

    public ChangeAffiliationAction(DefaultGame cardGame, Player player, AffiliatedCard card) {
        super(cardGame, player, "Change affiliation", ActionType.CHANGE_AFFILIATION);
        _performingCard = card;
        _selectAffiliationAction = new SelectAffiliationAction(
                cardGame, _performingPlayerId, getAffiliationOptions(cardGame));
        appendCost(_selectAffiliationAction);
    }

    public boolean requirementsAreMet(DefaultGame cardGame) {
        return !getAffiliationOptions(cardGame).isEmpty();
    }


    @Override
    public PhysicalCard getPerformingCard() { return _performingCard; }

    private List<Affiliation> getAffiliationOptions(DefaultGame cardGame) {
        List<Affiliation> _affiliationOptions = new LinkedList<>();
        _performingCard.getAffiliationOptions().forEach(affiliation -> {
            if (!_performingCard.isAffiliation(affiliation))
                _affiliationOptions.add(affiliation);
        });
        if (_performingCard instanceof PersonnelCard personnel) {
            if (personnel.getAttachedTo(cardGame) != null && personnel.getAttachedTo(cardGame) instanceof CardWithCrew cardWithCrew
                && cardWithCrew instanceof AffiliatedCard affiliatedCardWithCrew) {
                Collection<CardWithCompatibility> otherCards = new ArrayList<>();
                otherCards.add(affiliatedCardWithCrew);
                otherCards.addAll(cardWithCrew.getPersonnelInCrew(cardGame));
                _affiliationOptions.removeIf(affiliation ->
                        !personnel.isCompatibleWithOtherCardsAsAffiliation(affiliation, otherCards, (ST1EGame) cardGame));
            }
        } else if (_performingCard instanceof CardWithCrew cardWithCrew) {
            _affiliationOptions.removeIf(affiliation ->
                    !_performingCard.isCompatibleWithOtherCardsAsAffiliation(affiliation,
                            cardWithCrew.getPersonnelInCrew(cardGame), (ST1EGame) cardGame));
        } else {
            // There should be no other types of cards that would get the ChangeAffiliationAction
            _affiliationOptions.clear();
        }
        return _affiliationOptions;
    }

    @Override
    protected void processEffect(DefaultGame cardGame) {
        Affiliation selectedAffiliation = _selectAffiliationAction.getSelectedAffiliation();
        _performingCard.changeAffiliation((ST1EGame) cardGame, selectedAffiliation);
        setAsSuccessful();
    }

}