package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.discard.DiscardSingleCardAction;
import com.gempukku.stccg.actions.discard.RemoveDilemmaFromGameAction;
import com.gempukku.stccg.actions.missionattempt.EncounterSeedCardAction;
import com.gempukku.stccg.actions.missionattempt.FailDilemmaAction;
import com.gempukku.stccg.actions.playcard.PlayCardAction;
import com.gempukku.stccg.actions.playcard.STCCGPlayCardAction;
import com.gempukku.stccg.actions.scorepoints.ScorePointsAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.AttemptingUnit;
import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.common.filterable.Characteristic;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.AndFilterBlueprint;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.trigger.KilledTriggerChecker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unused")
public class Blueprint101_125 extends CardBlueprint {

    private final FilterBlueprint _killedPersonnelFilterBlueprint;

    // Klingon Death Yell
    Blueprint101_125() {
            // filter: a Klingon with Honor
        FilterBlueprint klingonFilter = (actionContext) -> Filters.Klingon;
        FilterBlueprint personnelFilter = (actionContext) -> Filters.personnel;
        FilterBlueprint honorFilter = (actionContext) -> Filters.and(SkillName.HONOR);
        _killedPersonnelFilterBlueprint = new AndFilterBlueprint(klingonFilter, personnelFilter, honorFilter);
    }

    @Override
    public List<TopLevelSelectableAction> getOptionalResponseActionsWhileInHand(PhysicalCard thisCard, Player player, ActionResult actionResult) {
        List<TopLevelSelectableAction> result = new ArrayList<>();
        if (thisCard instanceof ST1EPhysicalCard stCard) {
            Requirement playRequirement = new KilledTriggerChecker(_killedPersonnelFilterBlueprint);
            List<Requirement> playRequirements = List.of(playRequirement);
            ActionContext context = new DefaultActionContext(player.getPlayerId(), thisCard, actionResult);
            if (context.acceptsAllRequirements(playRequirements)) {
                try {
                    TopLevelSelectableAction playAction = new STCCGPlayCardAction(stCard, Zone.CORE, player, true);
                    playAction.appendEffect(new ScorePointsAction(thisCard.getGame(), thisCard, player, 5));
                    playAction.appendEffect(new DiscardSingleCardAction(thisCard, player, thisCard));
                    result.add(playAction);
                } catch(InvalidGameLogicException exp) {

                }
            }
        }
        return result;
    }
}