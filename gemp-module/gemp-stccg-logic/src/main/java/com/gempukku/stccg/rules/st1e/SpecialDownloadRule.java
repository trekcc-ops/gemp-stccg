package com.gempukku.stccg.rules.st1e;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.blueprints.ActionBlueprint;
import com.gempukku.stccg.cards.Skill;
import com.gempukku.stccg.cards.SpecialDownloadSkill;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

import java.util.ArrayList;
import java.util.List;

public class SpecialDownloadRule extends ST1ERule {

    public List<TopLevelSelectableAction> getPhaseActions(DefaultGame cardGame, Player player) {
        return getSpecialDownloadsThatPlayerCanInitiate(cardGame, player.getPlayerId());
    }

    @Override
    public List<TopLevelSelectableAction> getOptionalAfterActions(DefaultGame cardGame, String playerId,
                                                                  ActionResult actionResult) {
        if (actionResult.hasAnyType(List.of(
                ActionResult.Type.JUST_PLAYED,
                ActionResult.Type.START_OF_MISSION_ATTEMPT
        ))) {
            return getSpecialDownloadsThatPlayerCanInitiate(cardGame, playerId);
        } else {
            return new ArrayList<>();
        }
    }

    private List<TopLevelSelectableAction> getSpecialDownloadsThatPlayerCanInitiate(DefaultGame cardGame,
                                                                                    String playerName) {
        List<TopLevelSelectableAction> result = new ArrayList<>();
        for (PhysicalCard card : cardGame.getAllCardsInPlay()) {
            if (card instanceof PersonnelCard personnel) {
                for (Skill skill : personnel.getSkills(cardGame)) {
                    if (skill instanceof SpecialDownloadSkill downloadSkill) {
                        ActionBlueprint blueprint = downloadSkill.getActionBlueprint();
                        TopLevelSelectableAction action = blueprint.createAction(cardGame, playerName, card);
                        if (action != null) {
                            result.add(action);
                        }
                    }
                }
            }
        }
        return result;
    }

}