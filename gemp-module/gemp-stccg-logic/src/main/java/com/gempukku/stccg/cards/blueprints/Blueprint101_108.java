package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.playcard.PlayCardInitiationResult;
import com.gempukku.stccg.actions.playcard.STCCGPlayCardAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.filters.AndFilterBlueprint;
import com.gempukku.stccg.filters.FilterBlueprint;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.requirement.Requirement;
import com.gempukku.stccg.requirement.trigger.KilledTriggerChecker;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Blueprint101_108 extends CardBlueprint {

    // Amanda Rogers
    @Override
    public List<TopLevelSelectableAction> getOptionalResponseActionsWhileInHand(PhysicalCard thisCard, Player player, ActionResult actionResult) {
        List<TopLevelSelectableAction> result = new ArrayList<>();
        if (thisCard instanceof ST1EPhysicalCard stCard) {
            if (actionResult instanceof PlayCardInitiationResult playCardResult) {
                PhysicalCard cardToPlay = playCardResult.getCardToPlay();
                if (cardToPlay.getCardType() == CardType.INTERRUPT && cardToPlay != thisCard) {
                    TopLevelSelectableAction playAction = new STCCGPlayCardAction(stCard, Zone.CORE, player, true);
                    result.add(playAction);
                }
            }
        }
        return result;
    }
}