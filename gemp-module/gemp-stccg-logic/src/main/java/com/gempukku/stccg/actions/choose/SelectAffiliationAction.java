package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.DefaultEffect;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

import java.util.*;

public class SelectAffiliationAction extends ActionyAction {
    private final Collection<Affiliation> _affiliationOptions;
    private Affiliation _selectedAffiliation;
    private final PhysicalCard _performingCard;

    public SelectAffiliationAction(Player player, PhysicalCard performingCard,
                                   Collection<Affiliation> affiliationOptions) {
        super(player, ActionType.SELECT_AFFILIATION);
        _affiliationOptions = affiliationOptions;
        _performingCard = performingCard;
    }


    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return !_affiliationOptions.isEmpty();
    }

    @Override
    public Action nextAction(DefaultGame cardGame) {
        Map<String, Affiliation> affiliationStringMap = new HashMap<>();
        List<String> affiliationStrings = new ArrayList<>();
        for (Affiliation affiliation : _affiliationOptions) {
            affiliationStringMap.put(affiliation.getHumanReadable(), affiliation);
            affiliationStrings.add(affiliation.getHumanReadable());
        }
        cardGame.getUserFeedback().sendAwaitingDecision(
                new MultipleChoiceAwaitingDecision(cardGame.getPlayer(_performingPlayerId), "Choose an affiliation",
                        affiliationStrings) {
                    @Override
                    protected void validDecisionMade(int index, String result) {
                        _selectedAffiliation = affiliationStringMap.get(result);
                    }
                });
        return getNextAction();
    }

    @Override
    public PhysicalCard getPerformingCard() {
        return _performingCard;
    }

    @Override
    public PhysicalCard getCardForActionSelection() {
        return _performingCard;
    }

    public Affiliation getSelectedAffiliation() {
        return _selectedAffiliation;
    }
}