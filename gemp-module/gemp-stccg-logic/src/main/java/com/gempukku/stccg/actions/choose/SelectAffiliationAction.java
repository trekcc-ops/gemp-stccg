package com.gempukku.stccg.actions.choose;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.google.common.collect.Iterables;

import java.util.*;

public class SelectAffiliationAction extends ActionyAction {
    @JsonProperty("affiliationOptions")
    private final Collection<Affiliation> _affiliationOptions;
    @JsonProperty("selectedAffiliation")
    private Affiliation _selectedAffiliation;
    @JsonProperty("decisionId")
    @JsonIdentityReference(alwaysAsId=true)
    private AwaitingDecision _decision;


    public SelectAffiliationAction(DefaultGame cardGame, String performingPlayerName,
                                   Collection<Affiliation> affiliationOptions) {
        super(cardGame, performingPlayerName, ActionType.SELECT_AFFILIATION);
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

    protected void processEffect(DefaultGame cardGame) {
        if (_affiliationOptions.size() > 1 && _decision == null) {
            Map<String, Affiliation> affiliationStringMap = new HashMap<>();
            List<String> affiliationStrings = new ArrayList<>();
            for (Affiliation affiliation : _affiliationOptions) {
                affiliationStringMap.put(affiliation.getHumanReadable(), affiliation);
                affiliationStrings.add(affiliation.getHumanReadable());
            }
            _decision = new MultipleChoiceAwaitingDecision(_performingPlayerId, "Choose an affiliation",
                    affiliationStrings, cardGame) {
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
            cardGame.sendAwaitingDecision(_decision);
        }
    }


    public Affiliation getSelectedAffiliation() {
        return _selectedAffiliation;
    }

}