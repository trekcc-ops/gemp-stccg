package com.gempukku.stccg.actions.choose;

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
    protected final String _choiceText;
    private Integer _selectedSpacelineIndex = null;
    private final List<String> _selectionOptions = new ArrayList<>();

    public SelectMissionSeedIndexAction(DefaultGame cardGame, Player performingPlayer, List<Integer> options) {
        super(cardGame, performingPlayer, "Select location for mission", ActionType.MAKE_DECISION);
        _choiceText = "Select location for mission";
        for (Integer option : options) {
            _selectionOptions.add(String.valueOf(option));
        }
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    protected AwaitingDecision getDecision(DefaultGame cardGame) throws PlayerNotFoundException {
        Player performingPlayer = cardGame.getPlayer(_performingPlayerId);
        return new MultipleChoiceAwaitingDecision(performingPlayer,
                _selectionOptions, cardGame, DecisionContext.SEED_MISSION_INDEX_SELECTION) {
            @Override
            public void followUp() {
                _selectedSpacelineIndex = Integer.valueOf(_selectedValue);
            }
        };
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        AwaitingDecision decision = getDecision(cardGame);
        cardGame.getUserFeedback().sendAwaitingDecision(decision);
        setAsSuccessful();
        return getNextAction();
    }

    public Integer getSelectedIndex() throws DecisionResultInvalidException {
        if (_selectedSpacelineIndex == null) {
            throw new DecisionResultInvalidException("Got null response from SelectMissionSeedIndexAction");
        } else {
            return _selectedSpacelineIndex;
        }
    }

}