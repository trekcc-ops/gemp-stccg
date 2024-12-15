package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.actions.draw.DrawCardAction;
import com.gempukku.stccg.actions.playcard.PlayCardResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Blueprint156_010 extends CardBlueprint {

    // Surprise Party
    Blueprint156_010() {
        super("156_010");
    }

    public List<Action> getValidResponses(PhysicalCard thisCard, Player player, EffectResult effectResult) {
        List<Action> actions = new ArrayList<>();
        if (effectResult instanceof PlayCardResult playResult && playResult.getPlayedCard() == thisCard &&
                Objects.equals(thisCard.getGame().getOpponent(thisCard.getOwnerName()), player.getPlayerId())) {
            actions.add(new DrawCardAction(thisCard, player, 2));
        }
        return actions;
    }

    @Override
    public List<? extends Action> getGameTextActionsWhileInPlay(Player player, PhysicalCard card) {
        DefaultGame game = player.getGame();
        List<Action> actions = new LinkedList<>();
        Phase currentPhase = game.getCurrentPhase();
        if (currentPhase == Phase.END_OF_TURN && card.isControlledBy(player)) {
            actions.add(new DrawCardAction(card, player));
        }
        return actions;
    }

}