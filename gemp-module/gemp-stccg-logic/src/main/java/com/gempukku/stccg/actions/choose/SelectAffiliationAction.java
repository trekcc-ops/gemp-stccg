package com.gempukku.stccg.actions.choose;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.google.common.collect.Iterables;

import java.util.*;

public class SelectAffiliationAction extends ActionyAction {
    @JsonProperty("affiliationOptions")
    private final Collection<Affiliation> _affiliationOptions;
    private Affiliation _selectedAffiliation;
    private AwaitingDecision _decision;

    public SelectAffiliationAction(Player player, Collection<Affiliation> affiliationOptions) {
        super(player, ActionType.SELECT_AFFILIATION);
        _affiliationOptions = affiliationOptions;
        if (_affiliationOptions.size() == 1) {
            _selectedAffiliation = Iterables.getOnlyElement(_affiliationOptions);
            setAsSuccessful();
        }
    }


    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return !_affiliationOptions.isEmpty();
    }

    @Override
    public Action nextAction(DefaultGame cardGame) {
        if (_affiliationOptions.size() > 1 && _decision == null) {
            Map<String, Affiliation> affiliationStringMap = new HashMap<>();
            List<String> affiliationStrings = new ArrayList<>();
            for (Affiliation affiliation : _affiliationOptions) {
                affiliationStringMap.put(affiliation.getHumanReadable(), affiliation);
                affiliationStrings.add(affiliation.getHumanReadable());
            }
            _decision = new MultipleChoiceAwaitingDecision(
                    cardGame.getPlayer(_performingPlayerId), "Choose an affiliation", affiliationStrings) {
                @Override
                protected void validDecisionMade(int index, String result) {
                    _selectedAffiliation = affiliationStringMap.get(result);
                    if (_selectedAffiliation == null) {
                        setAsFailed();
                    } else {
                        setAsSuccessful();
                    }
                }
            };
            cardGame.getUserFeedback().sendAwaitingDecision(_decision);
        }

        return getNextAction();
    }


    public Affiliation getSelectedAffiliation() {
        return _selectedAffiliation;
    }

    public boolean wasCarriedOut() { return wasCompleted(); }

    @JsonProperty("decisionId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer getDecisionId() {
        if (_decision == null) {
            return null;
        } else {
            return _decision.getAwaitingDecisionId();
        }
    }
}