package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.actions.draw.DrawCardAction;
import com.gempukku.stccg.actions.playcard.PlayCardResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.modifiers.Modifier;

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
            actions.add(new DrawCardAction(thisCard, player));
            actions.add(new DrawCardAction(thisCard, player));
        }
        return actions;
    }

    @Override
    protected List<Modifier> getGameTextWhileActiveInPlayModifiers(Player player, final PhysicalCard thisCard) {
        List<Modifier> modifiers = new LinkedList<>();
        if (player == thisCard.getOwner()) {
            // allow extra card draw
        }
        return modifiers;
    }

}