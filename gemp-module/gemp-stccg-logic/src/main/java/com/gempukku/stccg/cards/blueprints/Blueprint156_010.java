package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.draw.DrawCardsAction;
import com.gempukku.stccg.actions.playcard.PlayCardResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.player.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Blueprint156_010 extends CardBlueprint {

    // Surprise Party
    Blueprint156_010() {
        super("156_010");
    }

    public List<TopLevelSelectableAction> getValidResponses(PhysicalCard thisCard, Player player,
                                                            ActionResult actionResult, DefaultGame cardGame) {
        String playerName = player.getPlayerId();
        List<TopLevelSelectableAction> actions = new ArrayList<>();
        if (actionResult instanceof PlayCardResult playResult && playResult.getPlayedCard() == thisCard &&
                Objects.equals(cardGame.getOpponent(thisCard.getOwnerName()), playerName)) {
            actions.add(new DrawCardsAction(thisCard, playerName, 2, cardGame));
        }
        return actions;
    }

    @Override
    public List<TopLevelSelectableAction> getGameTextActionsWhileInPlay(Player player, PhysicalCard card,
                                                                        DefaultGame game) {
        List<TopLevelSelectableAction> actions = new LinkedList<>();
        Phase currentPhase = game.getCurrentPhase();
        if (currentPhase == Phase.END_OF_TURN && card.isControlledBy(player)) {
            actions.add(new DrawCardsAction(game, card, player.getPlayerId()));
        }
        return actions;
    }

}