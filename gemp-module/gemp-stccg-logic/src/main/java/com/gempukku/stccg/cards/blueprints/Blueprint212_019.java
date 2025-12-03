package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.playcard.DownloadCardAction;
import com.gempukku.stccg.actions.usage.UseGameTextAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

import java.util.ArrayList;
import java.util.List;

public class Blueprint212_019 extends CardBlueprint {

    // Risk is Our Business

    // TODO - None of this is correct for the actual card's gametext. Using it as a test case for response actions

    public List<TopLevelSelectableAction> getValidResponses(PhysicalCard card, Player player,
                                                            ActionResult actionResult, DefaultGame cardGame) {
        List<TopLevelSelectableAction> actions = new ArrayList<>();
        if (actionResult.getType() == ActionResult.Type.START_OF_MISSION_ATTEMPT && card.isControlledBy(player)) {
            UseGameTextAction gameTextAction = new UseGameTextAction(cardGame, card, player, "Download a card");
            gameTextAction.appendEffect(new DownloadCardAction(cardGame, Zone.DRAW_DECK, player, CardType.PERSONNEL, card));
            actions.add(gameTextAction);
        }
        return actions;
    }

}