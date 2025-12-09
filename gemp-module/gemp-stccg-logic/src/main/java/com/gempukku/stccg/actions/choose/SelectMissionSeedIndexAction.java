package com.gempukku.stccg.actions.choose;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.AwaitingDecision;
import com.gempukku.stccg.decisions.DecisionContext;
import com.gempukku.stccg.decisions.MultipleChoiceAwaitingDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class SelectMissionSeedIndexAction extends ActionyAction {

    @JsonProperty("selectedIndex")
    private Integer _selectedIndex;

    @JsonProperty("selectionOptions")
    private final List<Integer> _selectionOptions;

    @JsonCreator
    private SelectMissionSeedIndexAction(@JsonProperty(value = "actionId", required = true)
                                             int actionId,
                                         @JsonProperty(value = "performingPlayerId", required = true)
                                         String performingPlayerName,
                                         @JsonProperty(value = "selectedIndex") Integer selectedIndex,
                                         @JsonProperty(value = "selectionOptions", required = true)
                                             List<Integer> selectionOptions
            ) {
        super(actionId, ActionType.MAKE_DECISION, performingPlayerName);
        _selectedIndex = selectedIndex;
        _selectionOptions = selectionOptions;
    }

    public SelectMissionSeedIndexAction(DefaultGame cardGame, Player performingPlayer, List<Integer> options) {
        super(cardGame, performingPlayer, "Select location for mission", ActionType.MAKE_DECISION);
        _selectionOptions = new ArrayList<>(options);
        _selectionOptions.addAll(options);
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    protected AwaitingDecision getDecision(DefaultGame cardGame) throws PlayerNotFoundException {
        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
        List<String> optionsToPass = new ArrayList<>();
        for (Integer option : _selectionOptions) {
            if (option != null) {
                optionsToPass.add(String.valueOf(option));
            }
        }
        return new MultipleChoiceAwaitingDecision(performingPlayer,
                optionsToPass, cardGame, DecisionContext.SEED_MISSION_INDEX_SELECTION) {
            @Override
            protected void validDecisionMade(int index, String result) {
                _selectedIndex = Integer.valueOf(result);
            }
        };
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        AwaitingDecision decision = getDecision(cardGame);
        cardGame.sendAwaitingDecision(decision);
        setAsSuccessful();
        return getNextAction();
    }

    @JsonIgnore
    public Integer getSelectedIndex() throws DecisionResultInvalidException {
        if (_selectedIndex == null) {
            throw new DecisionResultInvalidException("Got null response from SelectMissionSeedIndexAction");
        } else {
            return _selectedIndex;
        }
    }

}