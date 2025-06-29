package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.discard.DiscardSingleCardAction;
import com.gempukku.stccg.actions.discard.NullifyCardBeingPlayedAction;
import com.gempukku.stccg.actions.playcard.PlayCardInitiationResult;
import com.gempukku.stccg.actions.playcard.STCCGPlayCardAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.requirement.Requirement;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Blueprint101_108 extends CardBlueprint {

    // Amanda Rogers

    Blueprint101_108() {
        Requirement playRequirement = context -> {
            DefaultGame cardGame = context.getGame();
            return context.getSource().isInHand(cardGame);
        };
        appendPlayRequirement(playRequirement);
    }
    @Override
    public List<TopLevelSelectableAction> getOptionalResponseActionsWhileInHand(PhysicalCard thisCard, Player player, ActionResult actionResult) {
        List<TopLevelSelectableAction> result = new ArrayList<>();
        if (thisCard instanceof ST1EPhysicalCard stCard) {
            if (actionResult instanceof PlayCardInitiationResult playCardResult) {
                PhysicalCard cardToPlay = playCardResult.getCardToPlay();
                if (cardToPlay.getCardType() == CardType.INTERRUPT && cardToPlay != thisCard) {
                    TopLevelSelectableAction playAction = new STCCGPlayCardAction(stCard, Zone.CORE, player, true);
                    playAction.appendEffect(new NullifyCardBeingPlayedAction(thisCard, player, cardToPlay, playCardResult.getAction()));
                    playAction.appendEffect(new DiscardSingleCardAction(thisCard, player, thisCard));
                    result.add(playAction);
                }
            }
        }
        return result;
    }
}