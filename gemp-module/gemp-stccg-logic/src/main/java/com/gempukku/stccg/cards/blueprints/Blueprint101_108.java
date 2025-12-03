package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.discard.DiscardSingleCardAction;
import com.gempukku.stccg.actions.discard.NullifyCardBeingPlayedAction;
import com.gempukku.stccg.actions.playcard.PlayCardInitiationResult;
import com.gempukku.stccg.actions.playcard.STCCGPlayCardAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.requirement.ThisCardIsInHandRequirement;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Blueprint101_108 extends CardBlueprint {

    // Amanda Rogers

    Blueprint101_108() {
        appendPlayRequirement(new ThisCardIsInHandRequirement());
    }
    @Override
    public List<TopLevelSelectableAction> getOptionalResponseActionsWhileInHand(DefaultGame cardGame,
                                                                                PhysicalCard thisCard, Player player,
                                                                                ActionResult actionResult) {
        List<TopLevelSelectableAction> result = new ArrayList<>();
        if (thisCard instanceof ST1EPhysicalCard stCard &&
                actionResult instanceof PlayCardInitiationResult playCardResult) {
            PhysicalCard cardToPlay = playCardResult.getCardToPlay();
            if (cardToPlay.getCardType() == CardType.INTERRUPT && cardToPlay != thisCard) {
                TopLevelSelectableAction playThisCardAction =
                        new STCCGPlayCardAction(cardGame, stCard, Zone.CORE, player, true);
                ActionyAction actionToCancel = playCardResult.getAction();
                String playerName = player.getPlayerId();
                Action nullifyAction =
                        new NullifyCardBeingPlayedAction(cardGame, thisCard, playerName, cardToPlay, actionToCancel);
                Action discardNullifiedCardAction =
                        new DiscardSingleCardAction(cardGame, thisCard, player.getPlayerId(), thisCard);
                playThisCardAction.appendEffect(nullifyAction);
                playThisCardAction.appendEffect(discardNullifiedCardAction);
                result.add(playThisCardAction);
            }
        }
        return result;
    }
}