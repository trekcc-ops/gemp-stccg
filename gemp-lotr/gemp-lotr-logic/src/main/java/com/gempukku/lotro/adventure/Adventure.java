package com.gempukku.lotro.adventure;

import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.actions.SystemQueueAction;
import com.gempukku.lotro.modifiers.ModifiersLogic;
import com.gempukku.lotro.game.PlayerOrderFeedback;
import com.gempukku.lotro.processes.GameProcess;

import java.util.Set;

public interface Adventure {
    void applyAdventureRules(DefaultGame game, DefaultActionsEnvironment actionsEnvironment, ModifiersLogic modifiersLogic);

    GameProcess getStartingGameProcess(Set<String> players, PlayerOrderFeedback playerOrderFeedback);

    GameProcess getAfterFellowshipPhaseGameProcess();

    void appendNextSiteAction(SystemQueueAction action);

    GameProcess getAfterFellowshipArcheryGameProcess(int fellowshipArcheryTotal, GameProcess followingProcess);

    GameProcess getAfterFellowshipAssignmentGameProcess(Set<PhysicalCard> leftoverMinions, GameProcess followingProcess);

    GameProcess getBeforeFellowshipChooseToMoveGameProcess(GameProcess followingProcess);

    GameProcess getPlayerStaysGameProcess(DefaultGame game, GameProcess followingProcess);

    boolean isSolo();
}
