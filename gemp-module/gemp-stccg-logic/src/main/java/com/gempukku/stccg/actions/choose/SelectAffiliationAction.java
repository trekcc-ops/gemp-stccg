package com.gempukku.stccg.actions.choose;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.google.common.collect.Iterables;

import java.util.*;

public class SelectAffiliationAction extends ActionyAction {
    private final Collection<Affiliation> _affiliationOptions;
    private Affiliation _selectedAffiliation;

    public SelectAffiliationAction(Player player, Collection<Affiliation> affiliationOptions) {
        super(player, ActionType.SELECT_AFFILIATION);
        _affiliationOptions = affiliationOptions;
        if (_affiliationOptions.size() == 1) {
            _selectedAffiliation = Iterables.getOnlyElement(_affiliationOptions);
            _wasCarriedOut = true;
        }
    }


    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return !_affiliationOptions.isEmpty();
    }

    @Override
    public Action nextAction(DefaultGame cardGame) {
        if (_affiliationOptions.size() > 1) {
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
                            _wasCarriedOut = true;
                        }
                    });
        }

        return getNextAction();
    }


    public Affiliation getSelectedAffiliation() {
        return _selectedAffiliation;
    }

    public boolean wasCarriedOut() { return _wasCarriedOut; }
}